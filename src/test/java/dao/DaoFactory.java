package dao;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class DaoFactory {
    private static final String resource = "mybatis-config.xml";
    private static InputStream inputStream;
    static {
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    private static final SqlSession session = sessionFactory.openSession();;
    private DaoFactory(){};

    public static <T> T getDaoInstance(Class<T> daoClass){
        return session.getMapper(daoClass);
    }
}
