package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.Category;
import com.example.mall.mapper.CategoryMapper;
import com.example.mall.service.CategoryService;
import com.example.mall.vo.CategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务实现类（CategoryServiceImpl）
 * 这就是分类模块的"大脑"：接收 Controller 的参数，做业务判断，调 Mapper 存库
 * 
 * 分层铁律：
 * - 本类只调 Mapper，不直接写 SQL
 * - Mapper 只执行 SQL，不做业务判断
 * - 业务规则（查重、不能删除有子分类的分类）都在这里判断
 * 
 * 核心知识点：树形结构组装
 * 数据库里是平铺的（每个分类只有 parent_id），但前端要树形结构。
 * 所以 Service 层要"把平铺的数据组装成树"，这个逻辑在这里实现。
 */
@Service  // 告诉 Spring：这是一个 Service 实现类，交给 Spring 管理
public class CategoryServiceImpl implements CategoryService {

    /** 依赖注入：把 Mapper 交给 Spring，由 Spring 自动塞进来 */
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 获取所有分类（树形结构）
     * 
     * 流程（3 步）：
     * 1. 查所有分类（一次查询，不查 N 次）
     * 2. 在 Java 里组装成树（HashMap 查找父节点）
     * 3. 返回树形结构
     * 
     * 为什么不在 SQL 里直接返回树？
     * MySQL 没有原生的树形查询（PostgreSQL 有 WITH RECURSIVE，但 MySQL 不支持）。
     * 所以先查所有数据，然后在 Java 里组装，这样更快更灵活。
     */
    @Override
    public List<CategoryVO> list() {
        // 第 1 步：查所有分类
        List<Category> allCategories = categoryMapper.selectList(null);

        // 如果数据库里没数据，返回空列表（不返回 null，避免前端报错）
        if (allCategories == null || allCategories.isEmpty()) {
            return new ArrayList<>();
        }

        // 第 2 步：在 Java 里组装成树
        List<CategoryVO> rootCategories = new ArrayList<>();
        Map<Long, CategoryVO> categoryMap = new HashMap<>();

        // 先遍历所有分类，把每个分类转成 VO，放进 categoryMap
        for (Category category : allCategories) {
            CategoryVO vo = new CategoryVO(category);
            categoryMap.put(category.getId(), vo);
            
            // 顶级分类（parent_id=0）放进 rootCategories
            if (category.getParentId() == null || category.getParentId() == 0) {
                rootCategories.add(vo);
            }
        }

        // 第二次遍历：把每个非顶级分类挂到父节点的 children 里
        for (Category category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                continue;
            }
            
            CategoryVO parentVo = categoryMap.get(category.getParentId());
            
            if (parentVo != null) {
                parentVo.getChildren().add(categoryMap.get(category.getId()));
            }
        }

        // 第 3 步：返回顶级分类列表（树形结构已经组装好了）
        return rootCategories;
    }

    /**
     * 新增分类
     * 
     * 流程（3 步）：
     * 1. 校验参数（名称不能为空、parentId 不能为负数）
     * 2. 查重（同一父分类下，名称不能重复）
     * 3. 插入数据库
     * 
     * @Transactional：整个方法在同一个事务里，任何一步失败全部回滚
     */
    @Override
    @Transactional
    public Long add(Category category) {
        // 第 1 步：校验参数
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
        
        if (category.getParentId() != null && category.getParentId() < 0) {
            throw new BusinessException("父分类 id 不能为负数");
        }
        
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        if (category.getStatus() == null) {
            category.setStatus(0);
        }

        // 第 2 步：查重
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, category.getParentId())
                .eq(Category::getName, category.getName())
        );
        
        if (count > 0) {
            throw new BusinessException("该分类下已存在同名分类");
        }

        // 第 3 步：插入数据库
        categoryMapper.insert(category);
        return category.getId();
    }

    /**
     * 修改分类
     * 
     * 流程（3 步）：
     * 1. 校验分类是否存在
     * 2. 查重（修改后的名称不能和同级其他分类重复，但自己可以同名）
     * 3. 更新数据库
     */
    @Override
    @Transactional
    public void update(Category category) {
        // 第 1 步：校验分类是否存在
        Category existCategory = categoryMapper.selectById(category.getId());
        if (existCategory == null) {
            throw new BusinessException("分类不存在");
        }

        // 第 2 步：查重（排除自己）
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, category.getParentId())
                .eq(Category::getName, category.getName())
                .ne(Category::getId, category.getId())
        );
        
        if (count > 0) {
            throw new BusinessException("该分类下已存在同名分类");
        }

        // 第 3 步：更新数据库
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类
     * 
     * 流程（4 步）：
     * 1. 校验分类是否存在
     * 2. 检查是否有子分类（有则不能删）
     * 3. 检查是否有商品（暂时注释，等商品模块做完后加）
     * 4. 删除数据库记录
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 第 1 步：校验分类是否存在
        Category existCategory = categoryMapper.selectById(id);
        if (existCategory == null) {
            throw new BusinessException("分类不存在");
        }

        // 第 2 步：检查是否有子分类
        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id)
        );
        
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }

        // 第 3 步：检查是否有商品（暂时注释，等商品模块做完后加）
        // Long productCount = productMapper.selectCount(
        //     new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id)
        // );
        // if (productCount > 0) {
        //     throw new BusinessException("该分类下有商品，无法删除");
        // }

        // 第 4 步：删除数据库记录
        categoryMapper.deleteById(id);
    }
}
