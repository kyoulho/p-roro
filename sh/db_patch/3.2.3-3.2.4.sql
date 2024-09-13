create or replace view v_inventory_node
as
/*service info*/
select concat('SERV','-', service_id) as id
     , 'SERV' as type
     , service_id as type_id
     , service_name as name
     , 'SERV' as detail_type
     , 'ROOT' as parent_id
     , project_id
     , null as ip
     , 'Y' as is_inventory
     , null as engine_id
  from service_master
 where delete_yn = 'N'
 union all
/*server for service*/
select concat(im.inventory_type_code,'-', im.inventory_id) as id
     , im.inventory_type_code as type
     , im.inventory_id as type_id
     , im.inventory_name as name
     , 'INV' as detail_type
     , concat('SERV', '-', si.service_id) as parent_id
     , im.project_id
     , sm.representative_ip_address as ip
     , 'Y' as is_inventory
     , null as engine_id
  from inventory_master im
  join service_inventory si
    on si.inventory_id  = im.inventory_id
  join server_master sm
    on sm.server_inventory_id = im.inventory_id
 where im.inventory_type_code = 'SVR'
   and im.delete_yn = 'N'
 union all
/*middleware for server*/
select concat(im.inventory_type_code,'-', mi.middleware_instance_id) as id
     , im.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , mi.middleware_instance_name as name
     , mm.middleware_type_code as detail_type
     , concat('SVR', '-', im.server_inventory_id) as parent_id
     , im.project_id
     , sm.representative_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
  from inventory_master im
  join middleware_master mm
    on mm.middleware_inventory_id = im.inventory_id
  join server_master sm
    on sm.server_inventory_id = im.server_inventory_id
  join discovered_instance_master dim
    on dim.possession_inventory_id = im.inventory_id
  join middleware_instance mi
    on mi.middleware_instance_id = dim.discovered_instance_id
 where im.inventory_type_code = 'MW'
   and im.delete_yn = 'N'
   and dim.delete_yn = 'N'
 union all
/*database for server*/
select concat(im.inventory_type_code,'-', di.database_instance_id) as id
     , im.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , di.database_service_name as name
     , null as detail_type
     , concat('SVR', '-', im.server_inventory_id) as parent_id
     , im.project_id
     , dim.discovered_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
  from discovered_instance_master dim
  join inventory_master im
    on im.inventory_id = dim.possession_inventory_id
  join database_instance di
    on di.database_instance_id = dim.discovered_instance_id
 where dim.inventory_type_code = 'DBMS'
   and dim.delete_yn = 'N'
   and im.delete_yn = 'N'
 union all
/*application for middleware*/
select concat(dim.inventory_type_code,'-', midi.application_instance_id) as id
     , dim.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , im.inventory_name as name
     , im.inventory_detail_type_code as detail_type
     , concat('MW', '-', dima.discovered_instance_id) as parent_id
     , im.project_id
     , dim.discovered_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
  from discovered_instance_master dim
  join inventory_master im
    on im.inventory_id = dim.possession_inventory_id
   and dim.inventory_type_code = 'APP'
   and im.inventory_type_code = 'APP'
  join middleware_instance_application_instance midi
    on midi.application_instance_id = dim.discovered_instance_id
  join discovered_instance_master dima
    on dima.discovered_instance_id = midi.middleware_instance_id
 where dim.delete_yn = 'N'
   and im.delete_yn = 'N'
 union all
/*application for standalone*/
select concat(dim.inventory_type_code,'-', dim.discovered_instance_id) as id
     , dim.inventory_type_code as type
     , dim.discovered_instance_id as type_id
     , im.inventory_name as name
     , im.inventory_detail_type_code as detail_type
     , concat('SVR', '-', im.server_inventory_id) as parent_id
     , im.project_id
     , dim.discovered_ip_address as ip
     , 'Y' as is_inventory
     , im.inventory_id as engine_id
  from discovered_instance_master dim
  join inventory_master im
    on im.inventory_id = dim.possession_inventory_id
   and dim.inventory_type_code = 'APP'
   and im.inventory_type_code = 'APP'
   and im.inventory_detail_type_code = 'JAR'   
  left join middleware_instance_application_instance miai
    on miai.application_instance_id = dim.discovered_instance_id
 where miai.application_instance_id is null
;
