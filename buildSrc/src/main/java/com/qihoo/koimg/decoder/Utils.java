package com.qihoo.koimg.decoder;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {

  public static boolean match(String str, HashSet<Pattern> patterns) {
    if (patterns == null) {
      return false;
    }
    for(Pattern p : patterns) {
      Boolean isMatch = p.matcher(str).matches();
      if (isMatch) {
        return true;
      }
    }
    return false;
  }

}
