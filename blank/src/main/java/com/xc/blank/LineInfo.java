package com.xc.blank;

import static com.xc.blank.Status.NORMAL;

class LineInfo {

    public float start;
    public float end;
    public float lineTop;
    public boolean isSelect;
    public int index;//下划线对应的下划线集合的索引
    //public View view;//绑定的view，该view作为释放view状态用的
    public Status status = NORMAL;
}
