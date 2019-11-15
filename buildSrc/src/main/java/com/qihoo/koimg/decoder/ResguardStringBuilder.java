package com.qihoo.koimg.decoder;

import org.gradle.api.GradleException;

import java.util.*;
import java.util.regex.Pattern;

public class ResguardStringBuilder {
    private final List<String> mReplaceStringBuffer;
    private final Set<Integer> mIsReplaced;
    private final Set<Integer> mIsWhiteList;
    private String[] mAToZ = {
       "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
       "w", "x", "y", "z"
    };
    private String[] mAToAll = {
       "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "_", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
       "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };
    /**
     * 在window上面有些关键字是不能作为文件名的
     * CON, PRN, AUX, CLOCK$, NUL
     * COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9
     * LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, and LPT9.
     */
    private HashSet<String> mFileNameBlackList;

    public ResguardStringBuilder() {
      mFileNameBlackList = new HashSet<>();
      mFileNameBlackList.add("con");
      mFileNameBlackList.add("prn");
      mFileNameBlackList.add("aux");
      mFileNameBlackList.add("nul");
      mReplaceStringBuffer = new ArrayList<>();
      mIsReplaced = new HashSet<>();
      mIsWhiteList = new HashSet<>();
    }

    public void reset(HashSet<Pattern> blacklistPatterns) {
      mReplaceStringBuffer.clear();
      mIsReplaced.clear();
      mIsWhiteList.clear();

      for (int i = 0; i < mAToZ.length; i++) {
        String str = mAToZ[i];
        if (!Utils.match(str, blacklistPatterns)) {
          mReplaceStringBuffer.add(str);
        }
      }

      for (int i = 0; i < mAToZ.length; i++) {
        String first = mAToZ[i];
        for (int j = 0; j < mAToAll.length; j++) {
          String str = first + mAToAll[j];
          if (!Utils.match(str, blacklistPatterns)) {
            mReplaceStringBuffer.add(str);
          }
        }
      }

      for (int i = 0; i < mAToZ.length; i++) {
        String first = mAToZ[i];
        for (int j = 0; j < mAToAll.length; j++) {
          String second = mAToAll[j];
          for (int k = 0; k < mAToAll.length; k++) {
            String third = mAToAll[k];
            String str = first + second + third;
            if (!mFileNameBlackList.contains(str) && !Utils.match(str, blacklistPatterns)) {
              mReplaceStringBuffer.add(str);
            }
          }
        }
      }
    }

    // 对于某种类型用过的mapping，全部不能再用了
    public void removeStrings(Collection<String> collection) {
      if (collection == null) {
          return;
      }
      mReplaceStringBuffer.removeAll(collection);
    }

    public boolean isReplaced(int id) {
      return mIsReplaced.contains(id);
    }

    public boolean isInWhiteList(int id) {
      return mIsWhiteList.contains(id);
    }

    public void setInWhiteList(int id) {
      mIsWhiteList.add(id);
    }

    public void setInReplaceList(int id) {
      mIsReplaced.add(id);
    }

    public String getReplaceString() throws GradleException {
      if (mReplaceStringBuffer.isEmpty()) {
        throw new GradleException(String.format("now can only proguard less than 35594 in a single type\n"));
      }
      return mReplaceStringBuffer.remove(0);
    }
  }