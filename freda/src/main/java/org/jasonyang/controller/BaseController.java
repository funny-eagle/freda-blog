package org.jasonyang.controller;

import org.jasonyang.enumeration.PageEnum;
import org.jasonyang.model.Archive;
import org.jasonyang.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jason on 16/7/19.
 */
public class BaseController {
    @Autowired
    private ArchiveService archiveService;
    
    /**
     * 查询文档信息（返回 文档列表 archiveList 和 总页数 totalPages）
     * @param state
     * @param tag
     * @param page
     * @param pageSize
     * @return resMap archiveList, totalPages
     */
    public Map<String, Object> queryArchivesByPage(int state, String tag, Integer page, Integer pageSize) {
        final Map<String, Object> resMap = new HashMap<String, Object>();
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = PageEnum.PAGE_SIZE.getValue();
        }
        final int archivesCount = this.archiveService.countArchives(tag);
        if (archivesCount > 0) {
            //List<Archive> archiveList = this.archiveService.queryArchiveList(state, tag, page, pageSize);
            List<Archive> archiveList = this.archiveService.getAllArchivesInfo();
            if(archiveList != null && archiveList.size() > 0){
                if(archiveList.size() >= page * pageSize){
                    archiveList = archiveList.subList((page-1) * pageSize, page * pageSize);
                }else{
                    archiveList = archiveList.subList((page-1) * pageSize, archiveList.size());
                }
            }
            resMap.put("archiveList", archiveList);
            // 总页数(取天花板值 ) = 文档总数 / 每页个数 
            int totalPages = (int) Math.ceil((double) archivesCount / (double) pageSize);
            resMap.put("totalPages", totalPages);
            resMap.put("page", page);
        }
        return resMap;
    }
}