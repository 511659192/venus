package com.ym.materials.pool2;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by ym on 2018/6/10.
 */
public class JedisFactory extends BasePooledObjectFactory<Jedis> {

    public PooledObject<Jedis> makeObject(){
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.connect();
        System.out.println(jedis.isConnected());
        return  new DefaultPooledObject<Jedis>(jedis);
    }

    public void destroyObject(Jedis jedis){
        jedis.close();
    }

    public boolean validateObject(Jedis jedis) {
        if(jedis.isConnected()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public PooledObject<Jedis> wrap(Jedis arg0) {
        //TODO Auto-generated method stub
        return null;
    }

    @Override

    public Jedis create() throws Exception {
        //TODO Auto-generated method stub
        return null;

    }
}
