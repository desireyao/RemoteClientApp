package com.yaoh.picdiff.listeners;

import com.yaoh.picdiff.model.SliceModel;

import java.util.List;

/**
 * Created by yaoh on 2018/4/21.
 */

public interface ShotScreenPicDiffListener {

    public void onShotScreenPicDiff(boolean isSucceed, List<SliceModel> dataList);
}
