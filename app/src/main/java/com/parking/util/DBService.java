package com.parking.util;

import android.util.Log;

import com.parking.model.Message;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSpace;
import com.parking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class DBService {
    private Connection conn = null; //打开数据库对象
    private PreparedStatement ps = null;//操作整合sql语句的对象
    private ResultSet rs = null;//查询结果的集合

    //DBService 对象
    public static DBService dbService = null;

    // 构造方法，私有化
    private DBService() {

    }


    //获取MySQL数据库单例类对象
    public static DBService getDbService() {
        if (dbService == null) {
            dbService = new DBService();
        }
        return dbService;
    }

    // 插入数据
    public Observable<Integer> insertUserData(User user) {
        return Observable.just(user)
                .map(new Function<User, Integer>() {

                    @Override
                    public Integer apply(User user) throws Exception {
                        int result = -1;
                        conn = DBOpenHelper.getConn();
                        String sql = "INSERT INTO user (name,car,account,password) VALUES (?,?,?,?)";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, user.getName());
                        ps.setString(2, user.getCar());
                        ps.setString(3, user.getAccount());
                        ps.setString(4, user.getPassword());
                        result = ps.executeUpdate();
                        DBOpenHelper.closeAll(conn, ps);
                        return result;
                    }
                }).onErrorReturn(new Function<Throwable, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return -1;
                    }
                });

    }

    public Observable<Integer> updateUserData(User user) {
        return Observable.just(user)
                .map(new Function<User, Integer>() {

                    @Override
                    public Integer apply(User user) throws Exception {
                        int result = -1;
                        conn = DBOpenHelper.getConn();
                        String sql = "update user set account=?,password=?,car=?,name=? where id =?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, user.getAccount());
                        ps.setString(2, user.getPassword());
                        ps.setString(3, user.getCar());
                        ps.setString(4, user.getName());
                        ps.setInt(5, user.getId());
                        result = ps.executeUpdate();
                        DBOpenHelper.closeAll(conn, ps);
                        return result;
                    }
                }).onErrorReturn(new Function<Throwable, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return -1;
                    }
                });

    }

    public Observable<Integer> insertMessage(Message message) {
        return Observable.just(message)
                .map(new Function<Message, Integer>() {

                    @Override
                    public Integer apply(Message message) throws Exception {
                        int result = -1;
                        conn = DBOpenHelper.getConn();
                        String sql = "INSERT INTO message (content,type,date,sender_id,receiver_id) " +
                                "VALUES (?,?,?,?,?)";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, message.getContent());
                        ps.setInt(2, message.getType());
                        ps.setDate(3, message.getDate());
                        ps.setInt(4, message.getSender().getId());
                        ps.setInt(5, message.getReceiver().getId());
                        result = ps.executeUpdate();//返回1 执行成功
                        DBOpenHelper.closeAll(conn, ps);
                        return result;
                    }
                }).onErrorReturn(new Function<Throwable, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return -1;
                    }
                });

    }


    public Observable<List<ParkingLot>> getParkingLots() {
        return Observable.just(1)
                .map(new Function<Integer, List<ParkingLot>>() {

                    @Override
                    public List<ParkingLot> apply(Integer integer) throws Exception {
                       List<ParkingLot> list=new ArrayList<>();
                        conn = DBOpenHelper.getConn();
                        String sql = "SELECT * FROM parking_lot where 1=1";
                        ps = conn.prepareStatement(sql);
                        if (ps!=null){
                            rs = ps.executeQuery();
                            while (rs.next()){
                                ParkingLot parkingLot=new ParkingLot();
                                parkingLot.setLan(rs.getString("lan"));
                                parkingLot.setLat(rs.getString("lat"));
                                parkingLot.setLocation(rs.getString("location"));
                                parkingLot.setName(rs.getString("name"));
                                parkingLot.setId(rs.getInt("id"));
                                String sql2 = "SELECT * FROM parking_space where parking_lot_id = "+parkingLot.getId();
                                ResultSet resultSet=conn.prepareStatement(sql2).executeQuery();
                                List<ParkingSpace>parkingSpaces=new ArrayList<>();
                                while (resultSet.next()){
                                    ParkingSpace parkingSpace=new ParkingSpace();
                                    parkingSpace.setStartTime(resultSet.getTimestamp("startTime"));
                                    parkingSpace.setId(resultSet.getInt("id"));
                                    parkingSpace.setIs_empty(resultSet.getInt("is_empty"));
                                    parkingSpace.setNum(resultSet.getString("num"));
                                    parkingSpace.setParking_user_id(resultSet.getInt("parking_user_id"));
                                    parkingSpaces.add(parkingSpace);
                                }
                                parkingLot.setParkingSpaces(parkingSpaces);
                                list.add(parkingLot);
                            }
                        }
                        DBOpenHelper.closeAll(conn, ps);
                        return list;
                    }
                }).onErrorReturn(new Function<Throwable, List<ParkingLot>>() {
                    @Override
                    public List<ParkingLot> apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return new ArrayList<>();
                    }
                });

    }




    // 获取数据
    public Observable<Optional<User>> login(final User user) {
        return Observable.just(user)
                .map(new Function<User, Optional<User>>() {
                    @Override
                    public Optional apply(User user1) throws Exception {
                        conn = DBOpenHelper.getConn();
                        String sql = "select * from user where account = ? and password = ?";
                        if (conn != null && (!conn.isClosed())) {
                            ps = conn.prepareStatement(sql);
                            ps.setString(1, user.getAccount());
                            ps.setString(2, user.getPassword());
                            if (ps != null) {
                                rs = ps.executeQuery();
                                if (rs != null) {
                                    while (rs.next()) {
                                        User u = new User();
                                        u.setId(rs.getInt("id"));
                                        u.setName(rs.getString("name"));
                                        u.setCar(rs.getString("car"));
                                        u.setPassword(rs.getString("password"));
                                        u.setAccount(rs.getString("account"));
                                        return Optional.of(u);
                                    }
                                }
                            }
                        }
                        return Optional.empty();
                    }
                }).onErrorReturn(new Function<Throwable, Optional<User>>() {
                    @Override
                    public Optional apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return Optional.empty();
                    }
                });
    }


    public Observable<Integer> updateParkingSpace(final ParkingSpace parkingSpace) {
        return Observable.just(parkingSpace)
                .map(new Function<ParkingSpace, Integer>() {

                    @Override
                    public Integer apply(ParkingSpace parkingSpace) throws Exception {
                        int result = -1;
                        conn = DBOpenHelper.getConn();
                        String sql="update parking_space set is_empty=? ,parking_car=?,parking_user_id=?,startTime=? where id=?";
                        ps = conn.prepareStatement(sql);
                        ps.setInt(1, parkingSpace.getIs_empty());
                        ps.setString(2, parkingSpace.getParking_car());
                        ps.setInt(3, parkingSpace.getParking_user_id());
                        ps.setTimestamp(4, parkingSpace.getStartTime());
                        ps.setInt(5, parkingSpace.getId());
                        result = ps.executeUpdate();//返回1 执行成功
                        DBOpenHelper.closeAll(conn, ps);
                        return result;
                    }
                }).onErrorReturn(new Function<Throwable, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return -1;
                    }
                });
    }

    public Observable<Integer> getParkingSpaceByUserId(Integer userId) {
        return Observable.just(userId)
                .map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer userId) throws Exception {
                        int count =0;
                        conn = DBOpenHelper.getConn();
                        String sql = "SELECT COUNT(*) as t FROM parking_space where parking_user_id = "+userId;
                        ps = conn.prepareStatement(sql);
                        if (ps!=null){
                            rs = ps.executeQuery();
                            while (rs.next()){
                                count=rs.getInt("t");
                            }
                        }
                        DBOpenHelper.closeAll(conn, ps);
                        return count;
                    }
                }).onErrorReturn(new Function<Throwable,Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return 0;
                    }
                });

    }

    public Observable<List<Message>> getMessage(Integer userId) {
        return Observable.just(userId)
                .map(new Function<Integer, List<Message>>() {

                    @Override
                    public List<Message> apply(Integer userId) throws Exception {
                        List<Message> list=new ArrayList<>();
                        conn = DBOpenHelper.getConn();
                        String sql = "SELECT * FROM message left join user on user.id = message.sender_id where message.receiver_id= "+userId;
                        ps = conn.prepareStatement(sql);
                        if (ps!=null){
                            rs = ps.executeQuery();
                            while (rs.next()){
                                Message message=new Message();
                                User user=new User();
                                message.setContent(rs.getString("content"));
                                message.setDate(rs.getDate("date"));
                                user.setCar(rs.getString("car"));
                                user.setName(rs.getString("name"));
                                user.setId(rs.getInt("id"));
                                message.setSender(user);
                                list.add(message);
                            }
                        }
                        DBOpenHelper.closeAll(conn, ps);
                        return list;
                    }
                }).onErrorReturn(new Function<Throwable, List<Message>>() {
                    @Override
                    public List<Message> apply(Throwable throwable) {
                        throwable.printStackTrace();
                        DBOpenHelper.closeAll(conn, ps);
                        return new ArrayList<>();
                    }
                });

    }



}
