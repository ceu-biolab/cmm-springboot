SELECT * FROM compounds_view
WHERE mass BETWEEN (:lowerBound) AND (:upperBound);