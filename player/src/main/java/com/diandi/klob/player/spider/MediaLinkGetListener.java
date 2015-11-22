package com.diandi.klob.player.spider;

import java.util.List;

/**
 * *******************************************************************************
 * *********    Author : klob(kloblic@gmail.com) .
 * *********    Date : 2015-11-19  .
 * *********    Time : 14:16 .
 * *********    Version : 1.0
 * *********    Copyright © 2015, klob, All Rights Reserved
 * *******************************************************************************
 */
public interface MediaLinkGetListener {
    void onSuccess(List<MediaLink> links);

    void onFailure();

    void onRecommend(String url);
}
