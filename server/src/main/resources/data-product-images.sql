-- ============================================
-- 商品图片与详情数据（UPDATE 已有商品）
-- 图片来源：Unsplash（免费可商用）
-- ============================================

-- ==================== 数码家电-智能手机（category_id=1）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&q=80',
    detail = '<h3>iPhone 16 Pro Max 512GB</h3><p>搭载A18 Pro芯片，钛金属设计。</p>'
WHERE id = 3;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=800&q=80',
    detail = '<h3>iPhone 16 256GB</h3><p>A18芯片，超瓷晶面板。</p>'
WHERE id = 4;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&q=80',
    detail = '<h3>Samsung Galaxy S25 Ultra</h3><p>骁龙8 Elite 2亿像素。</p>'
WHERE id = 5;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1616348436168-de43ad0db179?w=800&q=80',
    detail = '<h3>Samsung Galaxy S25+</h3><p>骁龙8 Elite AMOLED屏。</p>'
WHERE id = 6;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80',
    detail = '<h3>华为 Mate 70 Pro</h3><p>麒麟9100 鸿蒙系统。</p>'
WHERE id = 7;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585060544812-6b45742d762f?w=800&q=80',
    detail = '<h3>华为 Pura 70 Ultra</h3><p>超聚光影像 昆仑玻璃。</p>'
WHERE id = 8;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80',
    detail = '<h3>小米15 Pro</h3><p>骁龙8 Elite 徕卡镜头。</p>'
WHERE id = 9;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80',
    detail = '<h3>小米15</h3><p>骁龙8 Elite 小尺寸旗舰。</p>'
WHERE id = 10;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=800&q=80',
    detail = '<h3>OPPO Find X8 Pro</h3><p>天玑9400 哈苏影像。</p>'
WHERE id = 11;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1567581935884-332906e4c0ae?w=800&q=80',
    detail = '<h3>vivo X200 Pro</h3><p>天玑9400 蔡司影像。</p>'
WHERE id = 12;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1512054502232-10a0a035d672?w=800&q=80',
    detail = '<h3>一加13</h3><p>骁龙8 Elite 2K屏。</p>'
WHERE id = 13;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=800&q=80',
    detail = '<h3>荣耀Magic7 Pro</h3><p>骁龙8 Elite 鹰眼相机。</p>'
WHERE id = 14;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585060544812-6b45742d762f?w=800&q=80',
    detail = '<h3>realme GT7 Pro</h3><p>骁龙8 Elite 潜望长焦。</p>'
WHERE id = 15;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80',
    detail = '<h3>iQOO 13</h3><p>骁龙8 Elite 2K电竞屏。</p>'
WHERE id = 16;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1591337676887-a217a6c6eee4?w=800&q=80',
    detail = '<h3>iPhone 15 128GB</h3><p>A16芯片 经典款。</p>'
WHERE id = 17;

-- ==================== 数码家电-笔记本电脑（category_id=1）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80',
    detail = '<h3>MacBook Pro 16 M4 Max</h3><p>48GB+1TB 深空黑。</p>'
WHERE id = 18;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=800&q=80',
    detail = '<h3>MacBook Air 15 M4</h3><p>16GB+512GB 午夜色。</p>'
WHERE id = 19;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&q=80',
    detail = '<h3>ThinkPad X1 Carbon 2025</h3><p>Ultra 7 32GB 超薄商务。</p>'
WHERE id = 20;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1593642702821-c8da6771f0c6?w=800&q=80',
    detail = '<h3>戴尔 XPS 15 2025</h3><p>Ultra 7 2.5K OLED屏。</p>'
WHERE id = 21;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&q=80',
    detail = '<h3>华为 MateBook X Pro</h3><p>Ultra 7 32GB 3.1K屏。</p>'
WHERE id = 22;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80',
    detail = '<h3>小米笔记本 Pro 16 2025</h3><p>Ultra 7 32GB 3.1K屏。</p>'
WHERE id = 23;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=800&q=80',
    detail = '<h3>华硕 ROG 幻16 Air</h3><p>Ultra 9 RTX5070 2.5K。</p>'
WHERE id = 24;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=800&q=80',
    detail = '<h3>联想拯救者 Y9000P 2025</h3><p>i9 RTX5070Ti 2.5K 240Hz。</p>'
WHERE id = 25;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&q=80',
    detail = '<h3>戴尔 U2723QE 4K显示器</h3><p>27英寸 IPS Black Type-C。</p>'
WHERE id = 26;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585792180666-f7347c490ee2?w=800&q=80',
    detail = '<h3>LG 27GP95R 4K电竞显示器</h3><p>27英寸 Nano IPS 160Hz。</p>'
WHERE id = 27;

-- ==================== 家居日用-大家电（category_id=3）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=800&q=80',
    detail = '<h3>格力 云佳 1.5匹变频空调</h3><p>新一级能效 静音运行。</p>'
WHERE id = 28;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&q=80',
    detail = '<h3>美的 风尊 2匹变频空调</h3><p>新一级能效 智能控制。</p>'
WHERE id = 29;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=800&q=80',
    detail = '<h3>海尔 10公斤滚筒洗衣机</h3><p>直驱变频 微蒸汽空气洗。</p>'
WHERE id = 30;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&q=80',
    detail = '<h3>西门子 9公斤洗烘一体机</h3><p>iDos智能投放 除菌洗。</p>'
WHERE id = 31;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?w=800&q=80',
    detail = '<h3>美的 501升法式多门冰箱</h3><p>一级能效 双循环 净味。</p>'
WHERE id = 32;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=800&q=80',
    detail = '<h3>海尔 550升对开门冰箱</h3><p>一级能效 干湿分储。</p>'
WHERE id = 33;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&q=80',
    detail = '<h3>戴森 V15 Detect 无线吸尘器</h3><p>激光检测灰尘 自动调节。</p>'
WHERE id = 34;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1600000000000-000000000001?w=800&q=80',
    detail = '<h3>科沃斯 X5 Pro 扫拖机器人</h3><p>全能基站 自动集尘洗拖布。</p>'
WHERE id = 35;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585515320310-259814833e62?w=800&q=80',
    detail = '<h3>松下 蒸烤一体机</h3><p>30L大容量 蒸烤炸三合一。</p>'
WHERE id = 36;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&q=80',
    detail = '<h3>飞利浦 空气净化器</h3><p>除甲醛 除菌 400㎡大空间。</p>'
WHERE id = 37;

-- ==================== 服饰鞋包-男装（category_id=2）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80',
    detail = '<h3>优衣库 男装 圆领T恤</h3><p>纯棉舒适 基础百搭。</p>'
WHERE id = 38;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80',
    detail = '<h3>Nike Dri-FIT 速干运动T恤</h3><p>透气排汗 经典logo。</p>'
WHERE id = 39;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800&q=80',
    detail = '<h3>Levi\'s 501 经典直筒牛仔裤</h3><p>美式经典 水洗蓝。</p>'
WHERE id = 40;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=800&q=80',
    detail = '<h3>优衣库 轻薄羽绒服</h3><p>90%白鸭绒 轻便保暖。</p>'
WHERE id = 41;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800&q=80',
    detail = '<h3>ZARA 女装 缎面连衣裙</h3><p>优雅气质 通勤百搭。</p>'
WHERE id = 42;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=800&q=80',
    detail = '<h3>波司登 极寒系列 长款羽绒服</h3><p>鹅绒充绒280g 抗寒-30℃。</p>'
WHERE id = 43;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1556821840-3a63f7722d68?w=800&q=80',
    detail = '<h3>H&M 男装 连帽卫衣</h3><p>棉质混纺 休闲舒适。</p>'
WHERE id = 44;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1556821840-3a63f7722d68?w=800&q=80',
    detail = '<h3>UNIQLO U 圆领卫衣</h3><p>厚实保暖 纯色简约。</p>'
WHERE id = 45;

-- ==================== 食品饮料-零食（category_id=4）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=800&q=80',
    detail = '<h3>三顿半 超即溶咖啡 精选装</h3><p>冷萃工艺 24颗混合装。</p>'
WHERE id = 46;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1523362628745-0c100150b504?w=800&q=80',
    detail = '<h3>农夫山泉 天然水 24瓶装</h3><p>550ml×24 长白山水源。</p>'
WHERE id = 47;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1606312619070-d48b4c652a52?w=800&q=80',
    detail = '<h3>良品铺子 坚果大礼包</h3><p>每日坚果30袋 混合装。</p>'
WHERE id = 48;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=800&q=80',
    detail = '<h3>伊利 金典纯牛奶 12盒装</h3><p>250ml×12 有机纯牛奶。</p>'
WHERE id = 49;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=800&q=80',
    detail = '<h3>元气森林 气泡水 12瓶装</h3><p>0糖0脂0卡 白桃味。</p>'
WHERE id = 50;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=800&q=80',
    detail = '<h3>瑞幸咖啡 耶加雪菲 500g</h3><p>中深烘焙 手冲精品。</p>'
WHERE id = 51;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1606312619070-d48b4c652a52?w=800&q=80',
    detail = '<h3>百草味 肉干大礼包</h3><p>猪肉脯牛肉干混合装。</p>'
WHERE id = 52;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=800&q=80',
    detail = '<h3>蒙牛 特仑苏 有机奶 10盒装</h3><p>250ml×10 有机纯牛奶。</p>'
WHERE id = 53;

-- ==================== 美妆个护-面部护肤（category_id=5）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800&q=80',
    detail = '<h3>兰蔻 小黑瓶精华液 50ml</h3><p>肌底修护 维稳抗初老。</p>'
WHERE id = 54;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?w=800&q=80',
    detail = '<h3>雅诗兰黛 小棕瓶精华 50ml</h3><p>修护肌肤 夜间修护。</p>'
WHERE id = 55;

UPDATE product SET 
    main_image = 'https://imagesᚖ.com/photo-1631729371254-42c2892f0e6e?w=800&q=80',
    detail = '<h3>SK-II 神仙水 230ml</h3><p>平衡肌肤 改善肤质。</p>'
WHERE id = 56;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800&q=80',
    detail = '<h3>资生堂 红腰子精华 75ml</h3><p>强韧肌肤 提升免疫力。</p>'
WHERE id = 57;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?w=800&q=80',
    detail = '<h3>海蓝之谜 精华面霜 30ml</h3><p>修护肌肤 奢华滋养。</p>'
WHERE id = 58;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800&q=80',
    detail = '<h3>MAC 魅可 子弹头口红</h3><p>经典正红 显色持久。</p>'
WHERE id = 59;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800&q=80',
    detail = '<h3>迪奥 999 口红</h3><p>传奇红唇 经典款。</p>'
WHERE id = 60;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=800&q=80',
    detail = '<h3>完美日记 动物眼影盘</h3><p>12色大地色系 日常百搭。</p>'
WHERE id = 61;

-- ==================== 家居日用-床上用品（category_id=3）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80',
    detail = '<h3>全棉时代 纯棉四件套</h3><p>100%全棉 裸睡级舒适。</p>'
WHERE id = 62;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1592789705501-f9ae4278a9e9?w=800&q=80',
    detail = '<h3>水星家纺 乳胶枕头</h3><p>泰国天然乳胶 护颈助眠。</p>'
WHERE id = 63;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800&q=80',
    detail = '<h3>苏泊尔 电饭煲 4L</h3><p>球釜IH 多功能预约。</p>'
WHERE id = 64;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=800&q=80',
    detail = '<h3>摩飞 多功能料理锅</h3><p>煎烤蒸煮 一锅搞定。</p>'
WHERE id = 65;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=800&q=80',
    detail = '<h3>北鼎 即热饮水机</h3><p>3秒即热 6段温控。</p>'
WHERE id = 66;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&q=80',
    detail = '<h3>追觅 吸尘器 V16</h3><p>185AW大吸力 无线手持。</p>'
WHERE id = 67;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1600369672770-985fd30004eb?w=800&q=80',
    detail = '<h3>洁丽雅 毛巾套装</h3><p>纯棉加厚 3条装。</p>'
WHERE id = 68;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=800&q=80',
    detail = '<h3>乐扣乐扣 保鲜盒套装</h3><p>耐热玻璃 5件套。</p>'
WHERE id = 69;

-- ==================== 运动户外-运动鞋服（category_id=6）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
    detail = '<h3>Nike Air Max 270 跑鞋</h3><p>气垫缓震 经典百搭。</p>'
WHERE id = 70;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&q=80',
    detail = '<h3>Adidas Ultraboost Light</h3><p>BOOST缓震 轻量回弹。</p>'
WHERE id = 71;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80',
    detail = '<h3>迪卡侬 双肩背包 30L</h3><p>防水面料 户外徒步。</p>'
WHERE id = 72;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1591291621164-2c6367723315?w=800&q=80',
    detail = '<h3>Keep 智能动感单车</h3><p>磁控静音 AI私教。</p>'
WHERE id = 73;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
    detail = '<h3>李宁 飞电3 Challenger</h3><p>碳板跑鞋 竞速训练。</p>'
WHERE id = 74;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&q=80',
    detail = '<h3>安踏 C202 GT 马拉松跑鞋</h3><p>氮科技 碳板竞速。</p>'
WHERE id = 75;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800&q=80',
    detail = '<h3>Yonex 天斧88D 羽毛球拍</h3><p>进攻型 高磅数。</p>'
WHERE id = 76;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800&q=80',
    detail = '<h3>迪卡侬 冲锋衣</h3><p>防水透湿 三合一。</p>'
WHERE id = 77;

-- ==================== 母婴玩具-婴儿用品（category_id=7）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1587654780291-39c9404d7dd0?w=800&q=80',
    detail = '<h3>乐高 城市系列 消防站</h3><p>培养动手能力 6岁+。</p>'
WHERE id = 78;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&q=80',
    detail = '<h3>费雪 学步车</h3><p>多功能 可坐可推。</p>'
WHERE id = 79;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&q=80',
    detail = '<h3>飞鹤 星飞帆 3段奶粉</h3><p>900g 新国标 适合12-36月。</p>'
WHERE id = 80;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=800&q=80',
    detail = '<h3>巴拉巴拉 童装套装</h3><p>纯棉舒适 男女童通用。</p>'
WHERE id = 81;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&q=80',
    detail = '<h3>babycare 婴儿湿巾</h3><p>80抽×6包 加厚绵柔。</p>'
WHERE id = 82;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&q=80',
    detail = '<h3>帮宝适 一级帮纸尿裤</h3><p>NB码96片 新生儿专用。</p>'
WHERE id = 83;

-- ==================== 服饰鞋包-鞋靴箱包（category_id=2）====================
UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
    detail = '<h3>新百伦 574 经典跑鞋</h3><p>复古百搭 舒适缓震。</p>'
WHERE id = 90;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1463100099107-aa0980c362e6?w=800&q=80',
    detail = '<h3>匡威 Chuck 70 高帮</h3><p>经典帆布鞋 复古潮。</p>'
WHERE id = 91;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800&q=80',
    detail = '<h3>Nike Dunk Low 熊猫</h3><p>经典配色 百搭板鞋。</p>'
WHERE id = 92;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80',
    detail = '<h3>Coach 蔻驰 单肩包</h3><p>经典C字logo 真皮。</p>'
WHERE id = 93;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1565026057447-bc90a3dceb87?w=800&q=80',
    detail = '<h3>新秀丽 行李箱 20寸</h3><p>PC材质 万向轮 登机箱。</p>'
WHERE id = 94;

UPDATE product SET 
    main_image = 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80',
    detail = '<h3>万里马 男士手提包</h3><p>头层牛皮 商务公文包。</p>'
WHERE id = 95;
