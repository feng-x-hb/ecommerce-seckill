-- ============================================
-- 数据库初始化脚本（schema.sql）
-- 策略：所有表一律 IF NOT EXISTS，绝不 DROP
-- ============================================

-- ==================== user 用户表 ====================
CREATE TABLE IF NOT EXISTS user (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(60)  NOT NULL,
    phone      VARCHAR(11)  DEFAULT NULL,
    nickname   VARCHAR(50)  DEFAULT NULL,
    avatar     VARCHAR(255) DEFAULT NULL,
    signature  VARCHAR(200) DEFAULT NULL,
    role       TINYINT      NOT NULL DEFAULT 0,
    status     TINYINT      NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== category 分类表 ====================
CREATE TABLE IF NOT EXISTS category (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id  BIGINT NOT NULL DEFAULT 0,
    name       VARCHAR(50) NOT NULL,
    sort       INT DEFAULT 0,
    status     TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 分类种子数据（7大类 + 子分类） ====================
-- 顶级分类
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '数码家电', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='数码家电' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '服饰鞋包', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='服饰鞋包' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '家居日用', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='家居日用' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '食品饮料', 3, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='食品饮料' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '美妆个护', 4, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='美妆个护' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '运动户外', 5, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='运动户外' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '母婴玩具', 6, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='母婴玩具' AND parent_id=0);

-- 数码家电 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '智能手机', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='智能手机' AND parent_id=1);
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '平板电脑', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='平板电脑' AND parent_id=1);
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '笔记本电脑', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='笔记本电脑' AND parent_id=1);
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '智能设备', 3, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='智能设备' AND parent_id=1);

-- 服饰鞋包 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '男装', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='男装' AND parent_id=2);
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '女装', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='女装' AND parent_id=2);
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '鞋靴', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='鞋靴' AND parent_id=2);
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '箱包', 3, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='箱包' AND parent_id=2);

-- 家居日用 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 3, '床上用品', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='床上用品' AND parent_id=3);
INSERT INTO category (parent_id, name, sort, status) SELECT 3, '厨具', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='厨具' AND parent_id=3);
INSERT INTO category (parent_id, name, sort, status) SELECT 3, '收纳整理', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='收纳整理' AND parent_id=3);

-- 食品饮料 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 4, '零食', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='零食' AND parent_id=4);
INSERT INTO category (parent_id, name, sort, status) SELECT 4, '饮品', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='饮品' AND parent_id=4);
INSERT INTO category (parent_id, name, sort, status) SELECT 4, '生鲜', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='生鲜' AND parent_id=4);

-- 美妆个护 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 5, '面部护肤', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='面部护肤' AND parent_id=5);
INSERT INTO category (parent_id, name, sort, status) SELECT 5, '彩妆', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='彩妆' AND parent_id=5);
INSERT INTO category (parent_id, name, sort, status) SELECT 5, '个人护理', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='个人护理' AND parent_id=5);

-- 运动户外 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 6, '运动鞋服', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='运动鞋服' AND parent_id=6);
INSERT INTO category (parent_id, name, sort, status) SELECT 6, '健身器材', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='健身器材' AND parent_id=6);
INSERT INTO category (parent_id, name, sort, status) SELECT 6, '户外装备', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='户外装备' AND parent_id=6);

-- 母婴玩具 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 7, '婴儿用品', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='婴儿用品' AND parent_id=7);
INSERT INTO category (parent_id, name, sort, status) SELECT 7, '玩具', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='玩具' AND parent_id=7);
INSERT INTO category (parent_id, name, sort, status) SELECT 7, '孕产护理', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='孕产护理' AND parent_id=7);

-- ==================== product 商品表 ====================
CREATE TABLE IF NOT EXISTS product (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id   BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    subtitle    VARCHAR(200) DEFAULT NULL,
    main_image  VARCHAR(255) DEFAULT NULL,
    images      TEXT DEFAULT NULL,
    detail      TEXT DEFAULT NULL,
    price       DECIMAL(10,2) NOT NULL,
    status      TINYINT DEFAULT 1,
    sales       INT DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_seller_id (seller_id),
    INDEX idx_category_id (category_id),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== sku 商品规格表 ====================
CREATE TABLE IF NOT EXISTS sku (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    specs      VARCHAR(255) NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    stock      INT NOT NULL DEFAULT 0,
    status     TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== cart_item 购物车表 ====================
CREATE TABLE IF NOT EXISTS cart_item (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    sku_id     BIGINT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    checked    TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== order 订单表 ====================
CREATE TABLE IF NOT EXISTS `order` (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no         VARCHAR(32) NOT NULL,
    user_id          BIGINT NOT NULL,
    status           TINYINT NOT NULL DEFAULT 0,
    total_amount     DECIMAL(10,2) NOT NULL,
    discount_amount  DECIMAL(10,2) DEFAULT 0,
    pay_amount       DECIMAL(10,2) NOT NULL,
    receiver_name    VARCHAR(50) NOT NULL,
    receiver_phone   VARCHAR(11) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    pay_time         DATETIME DEFAULT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== order_item 订单明细表 ====================
CREATE TABLE IF NOT EXISTS order_item (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       BIGINT NOT NULL,
    sku_id         BIGINT NOT NULL,
    product_name   VARCHAR(100) NOT NULL,
    product_image  VARCHAR(255) DEFAULT NULL,
    spec_desc      VARCHAR(255) DEFAULT NULL,
    price          DECIMAL(10,2) NOT NULL,
    quantity       INT NOT NULL,
    sub_total      DECIMAL(10,2) NOT NULL,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== seckill_activity 秒杀活动表 ====================
CREATE TABLE IF NOT EXISTS seckill_activity (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    title      VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time   DATETIME NOT NULL,
    status     TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== seckill_item 秒杀商品表 ====================
CREATE TABLE IF NOT EXISTS seckill_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id     BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    seckill_price   DECIMAL(10,2) NOT NULL,
    seckill_stock   INT NOT NULL,
    normal_price    DECIMAL(10,2) NOT NULL,
    purchase_limit  INT DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity_id (activity_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 秒杀演示数据 ====================
INSERT IGNORE INTO seckill_activity (id, title, start_time, end_time, status) VALUES
(1, '限时秒杀专场', '2026-08-29 00:00:00', '2026-12-31 23:59:59', 1);

INSERT IGNORE INTO seckill_item (id, activity_id, sku_id, seckill_price, seckill_stock, normal_price, purchase_limit) VALUES
(1, 1, 1, 8999.00, 20, 9999.00, 1),
(2, 1, 3, 4999.00, 15, 5999.00, 2),
(3, 1, 11, 8999.00, 10, 9999.00, 1),
(4, 1, 15, 7999.00, 12, 8999.00, 1),
(5, 1, 18, 5499.00, 20, 5999.00, 1),
(6, 1, 29, 2699.00, 15, 3299.00, 2),
(7, 1, 74, 499.00, 30, 599.00, 2),
(8, 1, 54, 799.00, 25, 950.00, 1);

-- ========== 迁移：添加 signature 列（已有则报错但不中断，continue-on-error=true） ==========
ALTER TABLE user ADD COLUMN signature VARCHAR(200) DEFAULT NULL AFTER avatar;

-- ==================== coupon_template 优惠券模板表 ====================
CREATE TABLE IF NOT EXISTS coupon_template (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    discount    DECIMAL(10,2) NOT NULL,
    min_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    total       INT NOT NULL DEFAULT 0,
    remain      INT NOT NULL DEFAULT 0,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      TINYINT DEFAULT 1,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== user_coupon 用户优惠券表 ====================
CREATE TABLE IF NOT EXISTS user_coupon (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    status      TINYINT DEFAULT 0,
    used_at     DATETIME DEFAULT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== review 评价表 ====================
CREATE TABLE IF NOT EXISTS review (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    order_id    BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    rating      TINYINT NOT NULL DEFAULT 5,
    content     TEXT,
    images      TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== favorite 收藏表 ====================
CREATE TABLE IF NOT EXISTS favorite (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 分类迁移（旧→新7大类） ====================
-- 清理旧子分类
DELETE FROM category WHERE parent_id IN (1,2,3,4,5,6,7,8,9,10,11) AND parent_id != 0;
-- 清理旧顶级分类（保留7个重命名）
DELETE FROM category WHERE parent_id = 0 AND name NOT IN ('数码家电','服饰鞋包','家居日用','食品饮料','美妆个护','运动户外','母婴玩具');
-- 更新7大类的sort
UPDATE category SET parent_id=0, sort=0 WHERE name='数码家电' AND parent_id IN (0,1);
UPDATE category SET parent_id=0, sort=1 WHERE name='服饰鞋包' AND parent_id IN (0,4);
UPDATE category SET parent_id=0, sort=2 WHERE name='家居日用' AND parent_id IN (0,7);
UPDATE category SET parent_id=0, sort=3 WHERE name='食品饮料' AND parent_id IN (0,5);
UPDATE category SET parent_id=0, sort=4 WHERE name='美妆个护' AND parent_id IN (0,6);
UPDATE category SET parent_id=0, sort=5 WHERE name='运动户外' AND parent_id IN (0,8);
UPDATE category SET parent_id=0, sort=6 WHERE name='母婴玩具' AND parent_id IN (0,9);
-- 删除不在7大类中的旧顶级分类
DELETE FROM category WHERE parent_id=0 AND name NOT IN ('数码家电','服饰鞋包','家居日用','食品饮料','美妆个护','运动户外','母婴玩具');
-- 更新商品category_id到新分类
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='数码家电' AND parent_id=0) AS t) WHERE category_id IN (1,2,3);
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='服饰鞋包' AND parent_id=0) AS t) WHERE category_id IN (4,11);
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='家居日用' AND parent_id=0) AS t) WHERE category_id=7;
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='食品饮料' AND parent_id=0) AS t) WHERE category_id=5;
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='美妆个护' AND parent_id=0) AS t) WHERE category_id=6;
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='运动户外' AND parent_id=0) AS t) WHERE category_id=8;
UPDATE product SET category_id=(SELECT id FROM (SELECT id FROM category WHERE name='母婴玩具' AND parent_id=0) AS t) WHERE category_id IN (9,10);
