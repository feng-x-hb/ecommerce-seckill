package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.Category;
import com.example.mall.mapper.CategoryMapper;
import com.example.mall.vo.CategoryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CategoryService 单元测试
 *
 * 测试策略：
 *   - 树组装：验证"平铺数据"能正确组装成"父子树形结构"
 *   - 新增：同级查重
 *   - 删除：有子分类时不能删
 *
 * 注意：这里不测数据库真实查询，只测 Service 层的业务逻辑。
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryMapper categoryMapper;

    // ========== 树组装测试 ==========

    @Test
    void list_empty() {
        // Arrange：数据库没有任何分类
        when(categoryMapper.selectList(null)).thenReturn(new ArrayList<>());

        // Act
        List<CategoryVO> result = categoryService.list();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "无数据时返回空列表而非 null");
    }

    @Test
    void list_treeStructure() {
        // Arrange：模拟 2 个顶级 + 2 个子级
        Category top1 = new Category();
        top1.setId(1L);
        top1.setParentId(0L);
        top1.setName("手机数码");

        Category top2 = new Category();
        top2.setId(2L);
        top2.setParentId(0L);
        top2.setName("电脑办公");

        Category child1 = new Category();
        child1.setId(3L);
        child1.setParentId(1L); // 属于"手机数码"
        child1.setName("智能手机");

        Category child2 = new Category();
        child2.setId(4L);
        child2.setParentId(1L); // 属于"手机数码"
        child2.setName("平板电脑");

        when(categoryMapper.selectList(null)).thenReturn(Arrays.asList(top1, top2, child1, child2));

        // Act
        List<CategoryVO> roots = categoryService.list();

        // Assert：应该有 2 个顶级
        assertEquals(2, roots.size(), "顶级分类应有 2 个");

        // "手机数码"下应有 2 个子级
        CategoryVO phoneCategory = roots.stream()
                .filter(c -> "手机数码".equals(c.getName()))
                .findFirst().orElseThrow();
        assertEquals(2, phoneCategory.getChildren().size(), "手机数码应有 2 个子级");

        // "电脑办公"下应有 0 个子级
        CategoryVO computerCategory = roots.stream()
                .filter(c -> "电脑办公".equals(c.getName()))
                .findFirst().orElseThrow();
        assertTrue(computerCategory.getChildren().isEmpty(), "电脑办公应无子级");
    }

    // ========== 新增测试 ==========

    @Test
    void add_success() {
        // Arrange：同名同父级不存在
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.insert(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(20L); // 模拟数据库自增回填 id
            return 1;
        });

        Category cat = new Category();
        cat.setName("新分类");
        cat.setParentId(0L);

        // Act
        Long id = categoryService.add(cat);

        // Assert
        assertNotNull(id);
        verify(categoryMapper).insert(any(Category.class));
    }

    @Test
    void add_duplicateName() {
        // Arrange：同名同父级已存在
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        Category cat = new Category();
        cat.setName("已存在分类");
        cat.setParentId(0L);

        // Act & Assert
        assertThrows(BusinessException.class, () -> categoryService.add(cat));
    }

    // ========== 删除测试 ==========

    @Test
    void delete_hasChildren() {
        // Arrange：分类存在，但有子分类
        Category cat = new Category();
        cat.setId(1L);
        when(categoryMapper.selectById(1L)).thenReturn(cat);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L); // 有 3 个子分类

        // Act & Assert：应抛异常
        assertThrows(BusinessException.class, () -> categoryService.delete(1L));
    }
}
