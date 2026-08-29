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

-- ==================== 分类种子数据 ====================
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '手机数码', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='手机数码' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '电脑办公', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='电脑办公' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '家用电器', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='家用电器' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '服装鞋包', 3, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='服装鞋包' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '食品饮料', 4, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='食品饮料' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '美妆护肤', 5, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='美妆护肤' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '家居日用', 6, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='家居日用' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '运动户外', 7, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='运动户外' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '母婴玩具', 8, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='母婴玩具' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '图书文具', 9, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='图书文具' AND parent_id=0);
INSERT INTO category (parent_id, name, sort, status) SELECT 0, '鞋靴箱包', 10, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='鞋靴箱包' AND parent_id=0);

-- 子分类
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '智能手机', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='智能手机' AND parent_id=1);
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '平板电脑', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='平板电脑' AND parent_id=1);
INSERT INTO category (parent_id, name, sort, status) SELECT 1, '手机配件', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='手机配件' AND parent_id=1);
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '笔记本电脑', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='笔记本电脑' AND parent_id=2);
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '台式机', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='台式机' AND parent_id=2);
INSERT INTO category (parent_id, name, sort, status) SELECT 2, '显示器', 2, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='显示器' AND parent_id=2);
INSERT INTO category (parent_id, name, sort, status) SELECT 3, '空调', 3, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='空调' AND parent_id=3);
INSERT INTO category (parent_id, name, sort, status) SELECT 3, '冰箱', 4, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='冰箱' AND parent_id=3);
INSERT INTO category (parent_id, name, sort, status) SELECT 3, '洗衣机', 5, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='洗衣机' AND parent_id=3);
INSERT INTO category (parent_id, name, sort, status) SELECT 6, '面部护肤', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='面部护肤' AND parent_id=6);
INSERT INTO category (parent_id, name, sort, status) SELECT 6, '彩妆', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='彩妆' AND parent_id=6);
INSERT INTO category (parent_id, name, sort, status) SELECT 7, '床上用品', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='床上用品' AND parent_id=7);
INSERT INTO category (parent_id, name, sort, status) SELECT 7, '厨具', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='厨具' AND parent_id=7);
INSERT INTO category (parent_id, name, sort, status) SELECT 8, '运动鞋服', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='运动鞋服' AND parent_id=8);
INSERT INTO category (parent_id, name, sort, status) SELECT 8, '健身器材', 1, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='健身器材' AND parent_id=8);

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
