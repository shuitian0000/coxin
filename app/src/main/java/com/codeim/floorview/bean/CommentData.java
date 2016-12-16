/**
 * 
 */
package com.codeim.floorview.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.codeim.floorview.utils.CommentComparator;
import com.codeim.floorview.utils.DateFormatUtils;
import com.codeim.coxin.R;

import android.content.Context;
import android.util.Log;

/**
 * @ClassName: 	CommentData
 * @Description:TODO
 * @author 	codeimShieh
 * @date	Feb 14, 2014		3:56:27 PM
 */
public class CommentData {
    
    private Context context ;
    
    public CommentData ( Context cnt ) {
        context = cnt ;
    }

    private Comment createComment ( int arrayId ) {
        String[] cmts = context.getResources ().getStringArray ( arrayId ) ;
        long parentId = Long.parseLong ( cmts[0] ) ;
        long userId = Long.parseLong ( cmts[1] ) ;
        long id = Long.parseLong ( cmts[2] ) ;
        Log.d ( "date", cmts[5] ) ;
        Date date = DateFormatUtils.parse ( cmts[5] ) ; 
        int floorNum = Integer.parseInt ( cmts[6] ) ;
        int lastNumForThisInfo = Integer.parseInt ( cmts[7] ) ;
        boolean is_available = Boolean.parseBoolean( cmts[8] ) ;
        return new Comment ( parentId, userId, id, cmts[3], cmts[4], date, floorNum, lastNumForThisInfo, is_available) ;
    }
    
    public List< Comment > getComments () {
        ArrayList < Comment > list = new ArrayList < Comment > () ;
        list.add ( createComment ( R.array.comment1 ) ) ;
        list.add ( createComment ( R.array.comment2 ) ) ;
        list.add ( createComment ( R.array.comment3 ) ) ;
        list.add ( createComment ( R.array.comment4 ) ) ;
        list.add ( createComment ( R.array.comment5 ) ) ;
        list.add ( createComment ( R.array.comment6 ) ) ;
        list.add ( createComment ( R.array.comment7 ) ) ;
        list.add ( createComment ( R.array.comment8 ) ) ;
        list.add ( createComment ( R.array.comment9 ) ) ;
        list.add ( createComment ( R.array.comment10 ) ) ;
        Collections.sort ( list, CommentComparator.getInstance () ) ;
        return list ;
    }
}
