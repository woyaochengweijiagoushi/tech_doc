package com.juege.tech_doc.service;

import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.juege.tech_doc.domain.Category;
import com.juege.tech_doc.mapper.CategoryMapper;
import com.juege.tech_doc.req.CategoryQueryReq;
import com.juege.tech_doc.req.CategorySaveReq;
import com.juege.tech_doc.resp.CategoryQueryResp;
import com.juege.tech_doc.resp.PageResp;
import com.juege.tech_doc.util.CopyUtil;
import com.juege.tech_doc.util.Snowflake;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class CategoryService {

	private static final Logger LOG = LoggerFactory.getLogger(CategoryService.class);

	@Resource
	private CategoryMapper categoryMapper;

	@Resource
	private Snowflake snowFlake;
	public List<CategoryQueryResp> all(String name) {

		final LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = Wrappers.lambdaQuery(Category.class)
				.orderByAsc(Category::getSort);
		final List<Category> categories = categoryMapper.selectList(categoryLambdaQueryWrapper);
		Set<Category> result=new LinkedHashSet<>();
		// 列表复制
		Map<Long, Category> categoryMap=categories.stream().collect(Collectors.toMap(Category::getId, category->category));
		Map<Long, List<Category>> categorySonMap=categories.stream().collect(Collectors.groupingBy(Category::getParent));
		List<Category> categoryList=categories.stream().filter(category -> category.getName().contains(name)).toList();
		result.addAll(categoryList);
		addSonCategory(result,categorySonMap,categoryList);
		addFaCategory(result,categoryMap,categoryList);
		return CopyUtil.copyList(result.stream().toList(), CategoryQueryResp.class);
	}
	private void addFaCategory(Set<Category> result,Map<Long, Category> categoryMap,List<Category> categoryList)
	{
		categoryList.stream().forEach(category -> {
			 if (categoryMap.containsKey(category.getParent()))
			{
				result.add(categoryMap.get(category.getParent()));
				addFaCategory(result,categoryMap,List.of(categoryMap.get(category.getParent())));
			}
		});
	}
	private void addSonCategory(Set<Category> result, Map<Long, List<Category>> categorySonMap, List<Category> categoryList)
	{
		categoryList.stream().forEach(category -> {
			if (categorySonMap.containsKey(category.getId()))
			{
				result.addAll(categorySonMap.get(category.getId()));
				addSonCategory(result,categorySonMap,categorySonMap.get(category.getId()));
			}
		});
	}

	public PageResp<CategoryQueryResp> list(CategoryQueryReq req) {

		final LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = Wrappers.lambdaQuery(Category.class).orderByAsc(Category::getSort);
		final Page<Category> categoryPage = PageDTO.of(req.getPage(), req.getSize());
		categoryMapper.selectPage(categoryPage, categoryLambdaQueryWrapper);

		LOG.info("总行数：{}", categoryPage.getTotal());
		LOG.info("总页数：{}", categoryPage.getPages());

		List<CategoryQueryResp> list = CopyUtil.copyList(categoryPage.getRecords(), CategoryQueryResp.class);

		PageResp<CategoryQueryResp> pageResp = new PageResp();
		pageResp.setTotal(categoryPage.getTotal());
		pageResp.setList(list);

		return pageResp;
	}

	/**
	 * 保存
	 */
	public void save(CategorySaveReq req) {
		Category category = CopyUtil.copy(req, Category.class);
		if (ObjectUtils.isEmpty(req.getId())) {
			// 新增
			category.setId(snowFlake.nextId());
			categoryMapper.insert(category);
		}
		else {
			// 更新
			categoryMapper.updateById(category);
		}
	}

	public void delete(Long id) {
		categoryMapper.deleteById(id);
	}
}
