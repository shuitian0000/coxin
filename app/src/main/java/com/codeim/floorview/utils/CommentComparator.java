/**
 * 
 */
package com.codeim.floorview.utils;

import java.util.Comparator;

import com.codeim.floorview.bean.Comment;

/**
 * @ClassName: 	CommentComparator
 * @Description:TODO
 * @author 	codeimShieh
 * @date	Feb 14, 2014		5:29:36 PM
 */
public class CommentComparator implements Comparator < Comment > {
    
    private static CommentComparator comparator = null ;
    
    private CommentComparator () {}
    
    public static CommentComparator getInstance () {
        return ( comparator == null ) ? ( comparator = new CommentComparator () ) : comparator ;
    }

    @Override
    public int compare ( Comment cmt1, Comment cmt2 ) {
        if ( cmt1.getDate ().after ( cmt2.getDate () ) ) {
            return -1 ;
        }else {
            return 1 ;
        }
    }

}
