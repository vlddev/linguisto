Databese scripts in this folder
================

install.sql - main install script, calls following scripts
-  create_db.sql
-  create_tables.sql
-  sample_data.sql - insert sample data to following tables: site_text, publications, daily_word
-  word_type.sql - list of word types (part of speech) for langugas in the dictionary
-  sample_dict.sql - insert sample data to following tables: inf, tr
-  sample_stats_yearly.sql - samle data (first 1000 rows) from ukrainian yearly frequency dictionary.
-  de_frequency.sql - german frequency dictionary
-  en_frequency.sql - english frequency dictionary
-  uk_frequency.sql - ukrainian frequency dictionary


-create_appuser.sql - 
-publications.sql
-site_text.sql

dict/ - whole dictionaries data [database tables: inf, tr, de_wf, en_wf]
-  inf_de.sql.gz
-  inf_en.sql.gz
-  inf_fr.sql.gz
-  tr_de.sql.gz
-  tr_en.sql.gz
-  tr_fr.sql.gz
-  de_wf.sql.gz
-  en_wf.sql.gz
