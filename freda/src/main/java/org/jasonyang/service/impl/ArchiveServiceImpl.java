package org.jasonyang.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jasonyang.enumeration.ArchiveStatus;
import org.jasonyang.mapper.ArchiveMapper;
import org.jasonyang.model.Archive;
import org.jasonyang.redis.RedisUtils;
import org.jasonyang.service.ArchiveService;
import org.jasonyang.utils.SerializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class ArchiveServiceImpl implements ArchiveService {
	private final Logger logger = Logger.getLogger(ArchiveServiceImpl.class);

	@Autowired
	private ArchiveMapper archiveMapper;

    public List<Archive> queryArchiveList(int state, String tag, Integer pageNum, Integer pageSize) {
    	Map<String, Object> paramsMap = new HashMap<String, Object>();
    	paramsMap.put("state", state);
    	paramsMap.put("tag", tag);
    	paramsMap.put("limit", pageSize);
    	paramsMap.put("offset", pageSize * (pageNum - 1));
		List<Archive> list = this.archiveMapper.selectArchives(paramsMap);
		return list;
	}

	/**
	 * 根据日期查找文章列表
	 * @param time
	 * @return
     */
	public List<Archive> queryArchiveListByCreateTime(String time){
		//List<Archive> list = this.ArchiveMapper.queryArchiveListByCreateTime(time);
		return null;
	}

	public int countArchives(Map<String, Object> paramsMap) {
		return archiveMapper.selectCountArchives(paramsMap);
	}

	public int saveArchive(Archive archive) {
		int resCount = 0;
		if (archive == null) return resCount;
		if(archive.getPreview() != null){
			//可以替换大部分空白字符， 不限于空格    
			// \s 可以匹配空格、制表符、换页符等空白字符的其中任意一个
			archive.setPreview(archive.getPreview().replaceAll("\\s*", ""));
		}
		if (StringUtils.isBlank(archive.getId())) {
			archive.setId(UUID.randomUUID().toString().replace("-", ""));
			archive.setCreateTime(new Date());
			resCount = this.archiveMapper.insertSelective(archive);
			if (resCount > 0) {
				this.logger.debug("====>新增文章 " + archive.getTitle() + "成功！");
			}
		} else {
			archive.setUpdateTime(new Date());
			resCount = this.archiveMapper.updateByPrimaryKeySelective(archive);
			if (resCount > 0) {
				this.logger.debug("====>修改文章 " + archive.getTitle() + "成功！");
			}
		}
		return resCount;
	}

	/**
	 * 根据ID查询文档
	 * @param id
	 * @param type 1：表示前台用，不查询markdown内容
     * @return
     */
	public Archive queryArchiveById(String id, int type) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		if(type == 1){
			return this.archiveMapper.selectArchiveById(id);
		}
		return this.archiveMapper.selectByPrimaryKey(id);
	}

    /**
     *
     */
	@Override
	@PostConstruct
	public void setAllPublishedArchivesInfoToRedis(){
		Jedis jedis = RedisUtils.getJedis();
		if(jedis == null){
			return;
		}
        Map paramsMap = new HashMap();
		paramsMap.put("state", ArchiveStatus.PUBLISHED.getValue());
		paramsMap.put("tag", null);
		paramsMap.put("limit", 0);
		paramsMap.put("offset", 0);
		jedis.set("allArchivesInfo".getBytes(), SerializeUtil.serializeList(this.archiveMapper.selectArchives(paramsMap)));
		jedis.close();
	}


	@Override
	public int deleteArchiveById(String id) {

		return this.archiveMapper.deleteByPrimaryKey(id);
	}

    /**
     * 从缓存中获取所有文档基本信息
     * @return archive list
     */
	@Override
	public List<Archive> getAllPublishedArchivesInfo(){
		Jedis jedis = RedisUtils.getJedis();
		if(jedis == null){
			return null;
		}
		List<Archive> archiveList = (List<Archive>) SerializeUtil.unserializeList(jedis.get("allArchivesInfo".getBytes()));
		jedis.close();
		return archiveList;
	}

}
