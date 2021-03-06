package com.team1.easyhelp.home.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Event;
import com.team1.easyhelp.receive.HelpReceiveMapActivity;
import com.team1.easyhelp.receive.SOSReceiveMapActivity;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class NeighborFragment extends Fragment {

    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private Context context;
    private View rootView;

    private MapView mapView;
    private BaiduMap mMap;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    boolean isFirstLoc = true;
    private BitmapDescriptor bitmap_help;
    private BitmapDescriptor bitmap_sos;

    private int user_id;
    private List<Event> events;

    private Gson gson = new Gson();


    public static NeighborFragment newInstance(String text) {
        NeighborFragment fragment = new NeighborFragment();
        Bundle args = new Bundle();
        args.putString(MAP_FRAGMENT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_neighbor, container, false);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        context = rootView.getContext();
        // 初始化视图
        initial();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setHasOptionsMenu(true); // 控制是否自定义本Fragment的ToolBar

        new Thread(new Runnable() {
            @Override
            public void run() {
                getNearbyEvents();
//                showNearbyEventsOnMap();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        // activity 销毁时同时销毁地图上相关的控件
        mLocClient.stop();
        mMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;

        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件与推送控件
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件与推送控件
        mapView.onResume();
    }

    private void initial() {
        // 初始化用户相关信息
        user_id = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        initialMap();
        initialIcon();
        initialLocation();
    }

    // 设定不同尺寸的标记图标资源
    private void initialIcon() {
        bitmap_sos = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_24dp);
        bitmap_help = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_indigo_a400_24dp);
    }

    // 初始化地图相关
    private void initialMap() {
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mMap = mapView.getMap();
        // 设为普通地图界面
        mMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 去除无关图标
        for (int i = 0; i < mapView.getChildCount(); i++) {
            View child = mapView.getChildAt(i);
            if (child instanceof ZoomControls || child instanceof ImageView)
                child.setVisibility(View.INVISIBLE);
        }
        // 设置地图初始缩放级别
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(15);
        mMap.animateMapStatus(u);
    }

    // 初始化定位控件
    private void initialLocation() {
        MyLocationConfiguration.LocationMode mCurrentMode =
                MyLocationConfiguration.LocationMode.NORMAL; //设置定位图层类型
        mMap.setMyLocationEnabled(true); // 开启定位图层
        /**
         * 配置定位图层显示方式；
         * 参数依次为定位图层显示方式， 是否允许显示方向信息，用户自定义定位图标
         */
        mMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, false, null));
        // 初始化定位控制器
        mLocClient = new LocationClient(context);
        // 设置定位监听器
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 开启GPS
        option.setCoorType("bd09ll"); // 设置坐标类型为百度经纬度标准
        option.setScanSpan(1000); // 设置每过1秒定位一次
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    // 设置定位的监听器
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不再处理新接收的位置
            if (location == null || mapView == null)
                return;
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            // 设置地图中心为当前定位点
            mMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(latitude, longitude);
                mMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    // 获取发生在周围的事件，初始化事件列表
    private void getNearbyEvents() {
        String jsonString = "{" +
                "\"id\":" + user_id +
                ",\"state\":0" + "}";
        String message = RequestHandler.sendPostRequest(
                "http://120.24.208.130:1501/event/get_nearby_event", jsonString);
        if (message.equals("false")) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "无法获取数据，请检查网络连接",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            try {
                JSONObject jO = new JSONObject(message);
                if (jO.getInt("status") == 500) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "获取周围事件失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                String jsonStringList = jO.getString("event_list");
                events = gson.fromJson(jsonStringList, new TypeToken<List<Event>>(){}
                        .getType());
                showNearbyEventsOnMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 将获取到的事件列表显示在地图上
    public void showNearbyEventsOnMap() {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for (Event event : events) {
                    LatLng eventLoc = new LatLng(event.getLatitude(), event.getLongitude());
                    // SOS事件标注红色标记，求助事件标注粉色标记
                    if (event.getType() == 2) {
                        mMap.addOverlay(new MarkerOptions().position(eventLoc).icon(bitmap_sos)
                                .title(Integer.toString(i)));
                    } else {
                        mMap.addOverlay(new MarkerOptions().position(eventLoc).icon(bitmap_help)
                                .title(Integer.toString(i)));
                    }
                    i++;
                }
                // 设置Marker点击的监听函数
                mMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int index = Integer.parseInt(marker.getTitle());
                        Intent intent;
                        if (events.get(index).getType() == 2) {
                            intent = new Intent(context,
                                    SOSReceiveMapActivity.class);
                        } else {
                            intent = new Intent(context,
                                    HelpReceiveMapActivity.class);
                        }

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", events.get(index));
                        intent.putExtras(bundle);

                        context.startActivity(intent);
                        return false;
                    }
                });
            }
        });
    }

}
