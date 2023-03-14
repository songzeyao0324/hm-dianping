package com.hmdp.service.impl;

import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.JsonStrListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShopTypeMapper shopTypeMapper;

    @Override
    public List<ShopType> queryList() {

        //1.查询redis中是否有商铺类型信息(这里使用list作为练习，使用String也是很好的)
        List<String> jsonStringList = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        //2.redis中有商铺类型信息，直接返回集合
        if (jsonStringList != null && jsonStringList.size()>0){
            List<ShopType> shopTypeList = JsonStrListUtil.toBeanList(jsonStringList, ShopType.class);
            return shopTypeList;
        }

        //3.redis中没有，则去数据库中查找
        List<ShopType> shopTypeList = shopTypeMapper.queryList();

        //4.查询到的信息集合存入到redis中(这里用到了自己写的JsonStrListUtil工具类)
        List<String> jsonStrList = JsonStrListUtil.toJsonStrList(shopTypeList);


        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY, jsonStrList);
        //5.返回商铺类型集合
        return shopTypeList;
    }
}
