package com.example.mall.service;

import com.example.mall.entity.Category;
import com.example.mall.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口（CategoryService）
 * 职责：定义"分类模块"对外提供的业务能力
 * 
 * 为什么要拆成接口 + 实现类？
 * 1. 面向接口编程：上层（Controller）只依赖接口，不依赖实现类
 * 2. 方便测试：可以 mock 接口，不用真正查数据库
 * 3. 方便扩展：以后可以换实现方式（比如从数据库改成 Redis）
 * 
 * 接口里只声明方法签名，具体实现放在 CategoryServiceImpl 里。
 */
public interface CategoryService {

    /**
     * 获取所有分类（树形结构）
     * 
     * 用途：前端画左侧导航栏、商品列表页的分类筛选
     * 返回：树形结构，顶级分类的 children 里有子分类，子分类的 children 里有孙分类...
     * 
     * 实现思路：
     * 1. 查所有分类（一次查询）
     * 2. 在 Java 里组装成树（HashMap 查找父节点）
     * 3. 返回树形结构
     * 
     * @return 分类树（List<CategoryVO>）
     */
    List<CategoryVO> list();

    /**
     * 新增分类
     * 
     * 业务规则：
     * 1. 分类名称不能为空
     * 2. 同一父分类下，分类名称不能重复
     * 3. 顶级分类的 parentId 为 0
     * 
     * @param category 要新增的分类（包含 name、parentId、sort）
     * @return 新分类的 id
     * @throws BusinessException 如果分类名称重复
     */
    Long add(Category category);

    /**
     * 修改分类
     * 
     * 业务规则：
     * 1. 分类必须存在
     * 2. 修改后的名称不能和同级其他分类重复
     * 3. 不能删除有子分类的分类（这个在 delete 里做）
     * 
     * @param category 要修改的分类（包含 id、name、sort）
     * @throws BusinessException 如果分类不存在或名称重复
     */
    void update(Category category);

    /**
     * 删除分类
     * 
     * 业务规则：
     * 1. 分类必须存在
     * 2. 不能删除有子分类的分类（删了父分类，子分类就悬空了）
     * 3. 不能删除有商品的分类（删了分类，商品就不知道属于哪类了）
     * 
     * @param id 要删除的分类 id
     * @throws BusinessException 如果分类不存在、有子分类或有商品
     */
    void delete(Long id);
}
