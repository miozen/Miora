INSERT INTO env_config (name, value, notes) VALUES
  ('baidu_statis', '{"site_id":"","access_token":""}', 'Analytics configuration'),
  ('email', '{"host":"","port":465,"password":"","username":""}', 'Mail configuration'),
  ('gaode_map_key', '{"key_code":"","security_code":""}', 'Map configuration'),
  ('gaode_coordinate', '{"key":""}', 'Map coordinate configuration'),
  ('baidu_statis_key', '{"key":""}', 'Public analytics key'),
  ('hcaptcha_key', '{"key":""}', 'Human verification configuration'),
  ('is_system_init', '{"value":false}', 'First-run setup state');

INSERT INTO web_config (name, value, notes) VALUES
  ('web', '{"icp":"","url":"","title":"Miora","footer":"","favicon":"","keyword":"Miora,blog","subhead":"A modern blog","create_time":0,"description":"Miora blog"}', 'Site configuration'),
  ('theme', '{"covers":[],"social":[],"dark_logo":"","light_logo":"","record_info":"","record_name":"Miora","swiper_text":[],"reco_article":[],"swiper_image":"","right_sidebar":[],"is_article_layout":"classics"}', 'Theme configuration'),
  ('other', '{}', 'Other configuration'),
  ('file', '{"upload_compress_mode":"auto"}', 'File configuration');
