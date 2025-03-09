package com.juege.tech_doc.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.juege.tech_doc.domain.Ebook;
import com.juege.tech_doc.mapper.EbookMapper;
import com.juege.tech_doc.req.EbookQueryReq;
import com.juege.tech_doc.req.EbookSaveReq;
import com.juege.tech_doc.resp.EbookQueryResp;
import com.juege.tech_doc.resp.PageResp;
import com.juege.tech_doc.util.CopyUtil;
import com.juege.tech_doc.util.Snowflake;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Service
public class EbookService {

	private static final Logger LOG = LoggerFactory.getLogger(EbookService.class);

	@Resource
	private EbookMapper ebookMapper;

	@Resource
	private Snowflake snowFlake;

	public PageResp<EbookQueryResp> list(EbookQueryReq req) {
		//EbookExample ebookExample = new EbookExample();
		//EbookExample.Criteria criteria = ebookExample.createCriteria();

		final LambdaQueryWrapper<Ebook> ebookLambdaQueryWrapper = Wrappers.lambdaQuery(Ebook.class)
				.like(StringUtils.hasText(req.getName()), Ebook::getName, req.getName())
				.eq(!ObjectUtils.isEmpty(req.getCategoryId2()), Ebook::getCategory2Id, req.getCategoryId2());

		final Page<Ebook> ebookPage = PageDTO.<Ebook>of(req.getPage(), req.getSize());
		ebookMapper.selectPage(ebookPage, ebookLambdaQueryWrapper);
		LOG.info("总行数：{}", ebookPage.getTotal());
		LOG.info("总页数：{}", ebookPage.getPages());

		// 列表复制
		List<EbookQueryResp> list = CopyUtil.copyList(ebookPage.getRecords(), EbookQueryResp.class);

		PageResp<EbookQueryResp> pageResp = new PageResp();
		pageResp.setTotal(ebookPage.getTotal());
		pageResp.setList(list);

		return pageResp;
	}

	/**
	 * 保存
	 */
	public void save(EbookSaveReq req) {
		Ebook ebook = CopyUtil.copy(req, Ebook.class);
		if (ObjectUtils.isEmpty(req.getId())) {
			// 新增
			ebook.setId(snowFlake.nextId());
			ebookMapper.insert(ebook);
		}
		else {
			// 更新
			ebookMapper.updateById(ebook);
		}
	}

	public void delete(Long id) {
		ebookMapper.deleteById(id);
	}
}
