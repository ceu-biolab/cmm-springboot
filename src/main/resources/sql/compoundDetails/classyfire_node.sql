SELECT c.node_id, d.node_name, c.kingdom, c.super_class, c.main_class, c.sub_class,
       c.level_5, c.level_6, c.level_7, c.level_8, c.level_9, c.level_10, c.created, c.last_updated
FROM classyfire_classification c
LEFT JOIN classyfire_classification_dictionary d ON d.node_id = c.node_id
WHERE c.node_id = ?;
