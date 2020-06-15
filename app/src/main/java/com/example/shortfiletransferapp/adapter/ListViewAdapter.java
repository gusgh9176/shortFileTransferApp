package com.example.shortfiletransferapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.shortfiletransferapp.R;
import com.example.shortfiletransferapp.vo.ListViewUserVO;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewUserVO> listViewItemList = new ArrayList<ListViewUserVO>() ;

    // ListViewAdapter의 생성자
    public ListViewAdapter() { }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }


    public static class UserViewHolder {
        TextView txtName;
        TextView txtExplain;

        public void bind(String name, String explain) {
            txtName.setText(name);
            txtExplain.setText(explain);
        }
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserViewHolder userViewHolder;
        final Context context = parent.getContext();

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.listview_item, parent, false);
            userViewHolder = new UserViewHolder();
            userViewHolder.txtName = convertView.findViewById(R.id.txt_value_name);
            userViewHolder.txtExplain = convertView.findViewById(R.id.txt_value_explain);
            convertView.setTag(userViewHolder);
        }
        else {
            userViewHolder = (UserViewHolder) convertView.getTag();
        }

        userViewHolder.bind(listViewItemList.get(position).getnameStr(),
                listViewItemList.get(position).getExplainStr());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 메소드. 개발자가 원하는대로 작성 가능.
    public void addItem(String index, String name, String explain) {
        ListViewUserVO item = new ListViewUserVO();

        item.setIndex(index);
//        item.setIconDrawable(icon);
        item.setNameStr(name);
        item.setExplainStr(explain);

        listViewItemList.add(item);
    }
}
