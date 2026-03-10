#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/deploy.sh --server <SERVER_IP> [options]

Options:
  --server <ip|host>          Target server (required)
  --upload-user <user>        SCP user (default: ceumass)
  --ssh-user <user>           SSH user for remote commands (default: upload user)
  --local-jar <path>          Local JAR path (default: target/cmm-1.0-SNAPSHOT.jar)
  --remote-tmp <path>         Remote temporary upload path (default: /tmp/<jar-name>)
  --remote-jar <path>         Final remote JAR path (default: /opt/cmm/cmm-springboot-be.jar)
  --service <name>            Systemd service name (default: cmm-springboot-be)
  --ssh-tty                   Force SSH pseudo-tty allocation
  --no-ssh-tty                Disable SSH pseudo-tty allocation
  --skip-build                Skip Maven build/repackage
  --dry-run                   Print commands without executing them
  -h, --help                  Show this help

Environment fallback:
  SERVER_IP, DEPLOY_UPLOAD_USER, SSH_DEPLOY_USER, LOCAL_JAR,
  REMOTE_TMP_JAR, REMOTE_JAR_PATH, SYSTEMD_SERVICE
EOF
}

SERVER_IP="${SERVER_IP:-}"
UPLOAD_USER="${DEPLOY_UPLOAD_USER:-ceumass}"
SSH_USER="${SSH_DEPLOY_USER:-}"
LOCAL_JAR="${LOCAL_JAR:-target/cmm-1.0-SNAPSHOT.jar}"
REMOTE_TMP_JAR="${REMOTE_TMP_JAR:-}"
REMOTE_JAR_PATH="${REMOTE_JAR_PATH:-/opt/cmm/cmm-springboot-be.jar}"
SYSTEMD_SERVICE="${SYSTEMD_SERVICE:-cmm-springboot-be}"
SSH_TTY_MODE="${SSH_TTY_MODE:-auto}" # auto|always|never
SKIP_BUILD=0
DRY_RUN=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --server)
      SERVER_IP="${2:-}"
      shift 2
      ;;
    --upload-user)
      UPLOAD_USER="${2:-}"
      shift 2
      ;;
    --ssh-user)
      SSH_USER="${2:-}"
      shift 2
      ;;
    --local-jar)
      LOCAL_JAR="${2:-}"
      shift 2
      ;;
    --remote-tmp)
      REMOTE_TMP_JAR="${2:-}"
      shift 2
      ;;
    --remote-jar)
      REMOTE_JAR_PATH="${2:-}"
      shift 2
      ;;
    --service)
      SYSTEMD_SERVICE="${2:-}"
      shift 2
      ;;
    --ssh-tty)
      SSH_TTY_MODE="always"
      shift
      ;;
    --no-ssh-tty)
      SSH_TTY_MODE="never"
      shift
      ;;
    --skip-build)
      SKIP_BUILD=1
      shift
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$SERVER_IP" ]]; then
  echo "Error: --server is required (or set SERVER_IP)." >&2
  usage
  exit 1
fi

if [[ -z "$SSH_USER" ]]; then
  SSH_USER="$UPLOAD_USER"
fi

if [[ -z "$REMOTE_TMP_JAR" ]]; then
  REMOTE_TMP_JAR="/tmp/$(basename "$LOCAL_JAR")"
fi

SSH_CMD=(ssh)
case "$SSH_TTY_MODE" in
  always)
    SSH_CMD+=(-tt)
    ;;
  never)
    ;;
  auto)
    if [[ -t 0 ]]; then
      SSH_CMD+=(-tt)
    fi
    ;;
  *)
    echo "Error: invalid SSH_TTY_MODE '$SSH_TTY_MODE' (expected auto|always|never)." >&2
    exit 1
    ;;
esac

run() {
  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "+ $*"
  else
    "$@"
  fi
}

if [[ "$SKIP_BUILD" -eq 0 ]]; then
  run mvn -DskipTests clean package spring-boot:repackage
fi

if [[ ! -f "$LOCAL_JAR" ]]; then
  echo "Error: local JAR not found: $LOCAL_JAR" >&2
  exit 1
fi

run scp "$LOCAL_JAR" "${UPLOAD_USER}@${SERVER_IP}:${REMOTE_TMP_JAR}"

REMOTE_CMD=$(
  cat <<EOF
set -euo pipefail
sudo -v
sudo mv "$REMOTE_TMP_JAR" "$REMOTE_JAR_PATH"
sudo systemctl restart "$SYSTEMD_SERVICE"
EOF
)

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "+ ${SSH_CMD[*]} ${SSH_USER}@${SERVER_IP} <<'REMOTE'"
  echo "$REMOTE_CMD"
  echo "REMOTE"
else
  "${SSH_CMD[@]}" "${SSH_USER}@${SERVER_IP}" "$REMOTE_CMD"
fi

echo "Deployment finished."
