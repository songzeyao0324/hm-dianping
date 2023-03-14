package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.hmdp.common.CustomException;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShopMapper shopMapper;

    @Override
    public Shop quaryById(Long id) {
        //缓存穿透
        //Shop shop = queryWithPassThrough(id);

        //缓存穿透 + 缓存击穿（基于互斥锁）
        Shop shop = queryWithMutex(id);


        return shop;
    }

    private Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        //1.从redis中查询商铺缓存(练习使用string，存入对象当然是使用hash更好)
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        //2.如果redis中存在，则直接返回数据
        if (StrUtil.isNotBlank(shopJson)){
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //处理缓存缓存穿透问题,不是null，是""
        if (shopJson != null){
            throw new CustomException("店铺不存在");
        }

        //3.redis中未命中，尝试获取互斥锁,解决缓存击穿
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean flag = tryLock(lockKey);
            //3-1判断是否获取锁
            if (!flag){
                //3-2如果未获取到锁,先休眠
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            //4.如果获取到锁，再次检测redis中是否有缓存，做DoubleCheck
            String shopJson2 = stringRedisTemplate.opsForValue().get(key);

            //5.如果redis中存在，则直接返回数据
            if (StrUtil.isNotBlank(shopJson2)){
                return JSONUtil.toBean(shopJson2, Shop.class);
            }
            //处理缓存缓存穿透问题,不是null，是""
            if (shopJson2 != null){
                throw new CustomException("店铺不存在");
            }

            // 6.查询数据库
            shop = shopMapper.selectById(id);

            //为了测试，查询到数据时休眠,模拟重建的延时
            Thread.sleep(200);
            //7.如果数据库中不存在，则返回失败信息,再redis中写入"",解决缓存穿透
            if (shop == null){
                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,MINUTES);
                throw new CustomException("店铺不存在");
            }
            //8.如果存在,将shop转为json字符串并存入redis中
            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL+RandomUtil.randomLong(5), MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //9.释放锁  有异常也要释放锁，所以放在finally中
            unlock(lockKey);
        }

        //10.返回商铺信息
        return shop;
    }



    private Shop queryWithPassThrough(Long id) {
        String key = CACHE_SHOP_KEY + id;
        //1.从redis中查询商铺缓存(练习使用string，存入对象当然是使用hash更好)
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        //2.如果redis中存在，则直接返回数据
        if (StrUtil.isNotBlank(shopJson)){
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //处理缓存缓存穿透问题,不是null，是""
        if (shopJson != null){
            throw new CustomException("店铺不存在");
        }
        //3.如果redis中不存在，则查询数据库
        Shop shop = shopMapper.selectById(id);
        //4.如果数据库中不存在，则返回失败信息,再redis中写入"",解决缓存穿透
        if (shop == null){
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,MINUTES);
            throw new CustomException("店铺不存在");
        }
        //5.如果存在,将shop转为json字符串并存入redis中

        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL+RandomUtil.randomLong(5), MINUTES);
        //6.返回商铺信息
        return shop;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Shop shop) {
        if (shop.getId() == null){
            throw new CustomException("店铺id不能为空");
        }
        //1.先修改数据库中的数据
        shopMapper.updateById(shop);
        //2.删除缓存中的数据
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
    }

    //获取锁
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    //释放锁
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }
}
