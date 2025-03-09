package com.juege.tech_doc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.juege.tech_doc.domain.Content;
import com.juege.tech_doc.domain.Doc;
import com.juege.tech_doc.exception.BusinessException;
import com.juege.tech_doc.exception.BusinessExceptionCode;
import com.juege.tech_doc.mapper.ContentMapper;
import com.juege.tech_doc.mapper.DocMapper;
import com.juege.tech_doc.mapper.DocMapperCust;
import com.juege.tech_doc.req.DocQueryReq;
import com.juege.tech_doc.req.DocSaveReq;
import com.juege.tech_doc.resp.DocQueryResp;
import com.juege.tech_doc.resp.PageResp;
import com.juege.tech_doc.util.CopyUtil;
import com.juege.tech_doc.util.RedisUtil;
import com.juege.tech_doc.util.RequestContext;
import com.juege.tech_doc.util.Snowflake;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class DocService {

	private static final Logger LOG = LoggerFactory.getLogger(DocService.class);

	@Resource
	private DocMapper docMapper;

	@Resource
	private DocMapperCust docMapperCust;

	@Resource
	private ContentMapper contentMapper;

	@Resource
	private Snowflake snowFlake;

	@Resource
	public RedisUtil redisUtil;

	@Resource
	public WsService wsService;

//	@Resource
//	private RocketMQTemplate rocketMQTemplate;

	public List<DocQueryResp> all(Long ebookId) {
		final LambdaQueryWrapper<Doc> docLambdaQueryWrapper = Wrappers.lambdaQuery(Doc.class).eq(Doc::getEbookId, ebookId)
				.orderByAsc(Doc::getSort);
		List<Doc> docList = docMapper.selectList(docLambdaQueryWrapper);

		// 列表复制
		List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

		return list;
	}

	public PageResp<DocQueryResp> list(DocQueryReq req) {
		final Page<Doc> docPage = PageDTO.of(req.getPage(), req.getSize());
		final LambdaQueryWrapper<Doc> queryWrapper = Wrappers.lambdaQuery(Doc.class)
				.orderByAsc(Doc::getSort);
		docMapper.selectPage(docPage, queryWrapper);

		LOG.info("总行数：{}", docPage.getTotal());
		LOG.info("总页数：{}", docPage.getPages());

		// 列表复制
		List<DocQueryResp> list = CopyUtil.copyList(docPage.getRecords(), DocQueryResp.class);
		PageResp<DocQueryResp> pageResp = new PageResp<>();
		pageResp.setTotal(docPage.getTotal());
		pageResp.setList(list);
		return pageResp;
	}

	/**
	 * 保存
	 */
	@Transactional
	public void save(DocSaveReq req) {
		Doc doc = CopyUtil.copy(req, Doc.class);
		Content content = CopyUtil.copy(req, Content.class);
		if (ObjectUtils.isEmpty(req.getId())) {
			// 新增
			doc.setId(snowFlake.nextId());
			//新增带了这俩字段的话 默认值不会生效 不带才会生效 不管是不是空
			doc.setViewCount(0);
			doc.setVoteCount(0);
			docMapper.insert(doc);

			content.setId(doc.getId());
			contentMapper.insert(content);
		}
		else {
			// 更新
			docMapper.updateById(doc);
			int count = contentMapper.updateById(content);
			if (count == 0) {
				contentMapper.insert(content);
			}
		}
	}

	public void delete(Long id) {
		docMapper.deleteById(id);
	}

	public void delete(List<String> ids) {
		docMapper.deleteByIds(ids);
	}

	public String findContent(Long id) {
		Content content = contentMapper.selectById(id);
		// 文档阅读数+1
		docMapperCust.increaseViewCount(id);
		if (ObjectUtils.isEmpty(content)) {
			return "";
		}
		else {
			return content.getContent();
		}
	}

	/**
	 * 点赞
	 */
	public void vote(Long id) {
		// docMapperCust.increaseVoteCount(id);
		// 远程IP+doc.id作为key，24小时内不能重复
		String ip = RequestContext.getRemoteAddr();
		if (redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + ip, 5000)) {
			docMapperCust.increaseVoteCount(id);
		}
		else {
			throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);
		}

		// 推送消息
		Doc docDb = docMapper.selectById(id);
		String logId = MDC.get("LOG_ID");
        wsService.sendInfo("【" + docDb.getName() + "】被点赞！", logId);
//		rocketMQTemplate.convertAndSend("VOTE_TOPIC", "【" + docDb.getName() + "】被点赞！");
	}

	public void updateEbookInfo() {
		docMapperCust.updateEbookInfo();
	}
}
