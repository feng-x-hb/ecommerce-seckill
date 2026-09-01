package com.example.mall.controller;

import com.example.mall.common.AdminOnly;
import com.example.mall.common.Result;
import com.example.mall.dto.CategoryDTO;
import com.example.mall.entity.Category;
import com.example.mall.service.CategoryService;
import com.example.mall.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器（CategoryController）—— 前台服务员
 * 职责红线：只负责"接请求 → 转给 Service → 把结果打包返回"，
 * 不写任何业务规则（查重、校验那些事都在 Service 里）。
 * 
 * 对应接口设计文档：
 * - GET /api/category/list → 获取分类树（前端画导航栏）
 * - POST /api/category → 新增分类（管理员）
 * - PUT /api/category/{id} → 修改分类（管理员）
 * - DELETE /api/category/{id} → 删除分类（管理员）
 */
@Tag(name = "分类", description = "商品分类查询")
@RestController  // 告诉 Spring：这是一个返回 JSON 的控制器
@RequestMapping("/api/category")  // 本类所有接口的公共前缀
public class CategoryController {

    /** 依赖注入：只依赖 CategoryService 接口，不关心它底层怎么实现 */
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取所有分类（树形结构）
     * 
     * 对应接口：GET /api/category/list
     * 用途：前端画左侧导航栏、商品列表页的分类筛选
     * 不需要登录（公开接口），前端刷新页面也能看到分类
     * 
     * 返回示例：
     * {
     *   "code": 200,
     *   "message": "ok",
     *   "data": [
     *     {
     *       "id": 1,
     *       "name": "手机数码",
     *       "children": [
     *         { "id": 2, "name": "智能手机", "children": [] },
     *         { "id": 3, "name": "平板电脑", "children": [] }
     *       ]
     *     }
     *   ]
     * }
     */
    @Operation(summary = "获取分类树")
    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> categories = categoryService.list();
        return Result.success(categories);
    }

    /**
     * 新增分类
     * 
     * 对应接口：POST /api/category
     * 用途：管理员新增分类
     * 需要登录 + 管理员权限（@AdminOnly）
     * 
     * 参数：CategoryDTO（@Valid 触发 DTO 上的校验注解，不通过直接 400）
     * 转换：DTO → Entity 后传给 Service（Controller 不直接操作 Entity）
     * 
     * 返回：新分类的 id
     */
    @Operation(summary = "新增分类")
    @AdminOnly
    @PostMapping
    public Result<Long> add(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setParentId(categoryDTO.getParentId() != null ? categoryDTO.getParentId() : 0L);
        category.setSort(categoryDTO.getSort() != null ? categoryDTO.getSort() : 0);
        Long id = categoryService.add(category);
        return Result.success(id);
    }

    /**
     * 修改分类
     * 
     * 对应接口：PUT /api/category/{id}
     * 用途：管理员修改分类信息
     * 需要登录 + 管理员权限（@AdminOnly）
     * 
     * 参数：id 从 URL 取，CategoryDTO 从请求体取
     * 转换：DTO → Entity 后传给 Service
     */
    @Operation(summary = "修改分类")
    @AdminOnly
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setId(id);
        category.setName(categoryDTO.getName());
        category.setParentId(categoryDTO.getParentId() != null ? categoryDTO.getParentId() : 0L);
        category.setSort(categoryDTO.getSort() != null ? categoryDTO.getSort() : 0);
        categoryService.update(category);
        return Result.success(null);
    }

    /**
     * 删除分类
     * 
     * 对应接口：DELETE /api/category/{id}
     * 用途：管理员删除分类
     * 需要登录 + 管理员权限（@AdminOnly）
     * 
     * 注意：id 从 URL 路径里取（@PathVariable）
     */
    @Operation(summary = "删除分类")
    @AdminOnly
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success(null);
    }
}
