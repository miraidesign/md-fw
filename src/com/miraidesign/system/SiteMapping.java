//------------------------------------------------------------------------
//@(#)SiteMapping.java
//      サイトマッピング、ページマッピング、ディレクトリマッピングと
//      それぞれの逆マッピング情報を提供する
//      ModuleManagerにインスタンスが登録されます
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.system;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;

/**
 *  システム全体の管理を行います。
 *  サイトマッピング、ページマッピング、ディレクトリマッピングと<br>
 *  それぞれの逆マッピング情報を提供する
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class SiteMapping {
    static private boolean debug      = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugTable = (SystemConst.debug && true);  // デバッグ表示

    // マッピングセクション名称
    static final String secSiteMapping      = "SiteMapping";
    static final String secPageMapping      = "PageMapping";
    static final String secCMSMapping       = "CMSMapping";
    static final String secDirectoryMapping = "DirectoryMapping";

    // site用マッピングテーブル HashVector:CharArray/IntQueue(後優先)
    HashVector<CharArray,IntQueue> siteMapping;
    HashVector<CharArray,IntQueue> pageMapping;
    // ディレクトリマッピングテーブル HashVector:CharArray/CharArrayQueue(後優先)
    HashVector<CharArray,CharArrayQueue> dirMapping;
    
    // 逆マッピングテーブル     HashVector:IntObject/CharArrayQueue(後優先）
    HashVector<IntObject,CharArrayQueue> siteIDMapping;
    HashVector<IntObject,CharArrayQueue> pageIDMapping;
    HashVector<IntObject,CharArrayQueue> pageIDMappingToContent;
    
    HashParameter pageIDMappingToUser;
    
    public static SiteMapping getInstance(SessionObject session) {
        SiteMapping siteMapping = null;
        do {
            if (session == null) break;
            ModuleManager mm = session.getModuleManager();
            if (mm == null) break;
            siteMapping = mm.getSiteMapping();
        } while (false);
        return siteMapping;
    }
    
    public HashParameter getUserPageMapping() {
        if (pageIDMappingToUser == null) pageIDMappingToUser = new HashParameter();
        return pageIDMappingToUser;
    }
    public static HashParameter getUserPageMapping(SessionObject session) {
        SiteMapping siteMapping = getInstance(session);
        if (siteMapping == null) return null;
        return siteMapping.getUserPageMapping();
    }
    
    ModuleManager mm;
    //-------------------------------------------------------------------------
    // consuructor
    //-------------------------------------------------------------------------
    public  SiteMapping(ModuleManager mm) {  // site.iniから情報を取得する
        init(mm);
    }
    
    // site.iniの情報を読み出す
    //                           CMSマッピング追加
    private void init(ModuleManager mm) {
        if (debug) System.out.println("▽SiteMapping#init() start--------------------");
        if (mm != null && mm.ini != null) {
            this.mm = mm;
            HashVector _page = mm.ini.getKeyTable(mm.getSection(secPageMapping));
            HashVector _cms  = mm.ini.getKeyTable(mm.getSection(secCMSMapping));
            HashVector _dir  = mm.ini.getKeyTable(mm.getSection(secDirectoryMapping));
            
            if (_page == null || _page.size() == 0) {
                //if (debug) System.out.println("SiteMapping#init() Page Mapping not found in \"site.ini\".");
            } else {
                if (debug) System.out.println("PageMapping size="+_page.size());
                if (_page.size() > 0) {
                    if (pageMapping   == null) pageMapping   = new HashVector<CharArray,IntQueue>();
                    if (pageIDMapping == null) pageIDMapping = new HashVector<IntObject,CharArrayQueue>();
                }
                for (int i = 0; i < _page.size(); i++) {
                    CharArray      _key  =  (CharArray)_page.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_page.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            append(pageMapping, _key, _queue.peek(j).getInt());
                            append(pageIDMapping, _queue.peek(j).getInt(), _key);
                        }
                    }
                }
            }
            if (_cms == null || _cms.size() == 0) {
                //if (debug) System.out.println("SiteMapping#init() CMS Mapping not found in \"site.ini\".");
            } else {
                if (debug) System.out.println("CMSMapping size="+_cms.size());
                if (_cms.size() > 0) {
                    //if (pageMapping   == null) pageMapping   = new HashVector<CharArray,IntQueue>();
                    if (pageIDMappingToContent == null) pageIDMappingToContent = new HashVector<IntObject,CharArrayQueue>();
                }
                for (int i = 0; i < _cms.size(); i++) {
                    CharArray      _key  =  (CharArray)_cms.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_cms.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            //append(pageMapping, _key, _queue.peek(j).getInt());
                            append(pageIDMappingToContent, _queue.peek(j).getInt(), _key);
                        }
                    }
                }
            }
            
            if (_dir == null || _dir.size() == 0) {
                //if (debug) System.out.println("SiteMapping#init() Dir Mapping not found in \"site.ini\".");
            } else {
                if (debug) System.out.println("DirectoryMapping size="+_dir.size());
                if (_dir.size() > 0) {
                    if (dirMapping == null) dirMapping = new HashVector<CharArray,CharArrayQueue>();
                }
                for (int i = 0; i < _dir.size(); i++) {
                    CharArray      _key  =  (CharArray)_dir.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_dir.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            append(dirMapping, _key, _queue.peek(j));
                        }
                    }
                }
            }
        } else {
            System.out.println("SiteMapping#init() ModuleManager("+(mm != null)+").ini not found !!");
        }
        
        if (SystemManager.ini != null) {
            HashVector _site = SystemManager.ini.getKeyTable("["+secSiteMapping+"]");
            HashVector _page = SystemManager.ini.getKeyTable("["+secPageMapping+"]");
            HashVector _cms  = SystemManager.ini.getKeyTable("["+secCMSMapping+"]");
            HashVector _dir  = SystemManager.ini.getKeyTable("["+secDirectoryMapping+"]");
            
            if (_site == null || _site.size() == 0) {
                //if (debug) System.out.println("SiteMapping#init() Site Mapping not found in \"system.ini\".");
            } else {
                if (debug) System.out.println("SiteMapping size="+_site.size());
                if (_site.size() > 0) {
                    if (siteMapping   == null) siteMapping   = new HashVector<CharArray,IntQueue>();
                    if (siteIDMapping == null) siteIDMapping = new HashVector<IntObject,CharArrayQueue>();
                }
                for (int i = 0; i < _site.size(); i++) {
                    CharArray      _key  =  (CharArray)_site.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_site.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            append(siteMapping,   _key, _queue.peek(j).getInt());
                            append(siteIDMapping, _queue.peek(j).getInt(), _key);
                        }
                    }
                }
            }
            
            if (_page == null || _page.size() == 0) {
                //if (debug) System.out.println("SiteMapping#init() Page Mapping not found in \"system.ini\".");
            } else {
                if (debug) System.out.println("PageMapping size="+_page.size());
                if (_page.size() > 0) {
                    if (pageMapping   == null) pageMapping   = new HashVector<CharArray,IntQueue>();
                    if (pageIDMapping == null) pageIDMapping = new HashVector<IntObject,CharArrayQueue>();
                }
                for (int i = 0; i < _page.size(); i++) {
                    CharArray      _key  =  (CharArray)_page.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_page.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            append(pageMapping, _key, _queue.peek(j).getInt());
                            append(pageIDMapping, _queue.peek(j).getInt(), _key);
                        }
                    }
                }
            }
            if (_cms == null || _cms.size() == 0) {
                //if (debug) System.out.println("SiteMapping#init() CMS Mapping not found in \"system.ini\".");
            } else {
                if (debug) System.out.println("CMSMapping size="+_cms.size());
                if (_cms.size() > 0) {
                    //if (pageMapping   == null) pageMapping   = new HashVector<CharArray,IntQueue>();
                    if (pageIDMappingToContent == null) pageIDMappingToContent = new HashVector<IntObject,CharArrayQueue>();
                }
                for (int i = 0; i < _cms.size(); i++) {
                    CharArray      _key  =  (CharArray)_cms.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_cms.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            //append(pageMapping, _key, _queue.peek(j).getInt());
                            append(pageIDMappingToContent, _queue.peek(j).getInt(), _key);
                        }
                    }
                }
            }
            
            if (_dir == null || _dir.size() == 0) {
                if (debug) System.out.println("SiteMapping#init() Dir Mapping not found in \"system.ini\".");
            } else {
                if (debug) System.out.println("DirectoryMapping size="+_dir.size());
                if (_dir.size() > 0) {
                    if (dirMapping   == null) dirMapping   = new HashVector<CharArray,CharArrayQueue>();
                }
                for (int i = 0; i < _dir.size(); i++) {
                    CharArray      _key  =  (CharArray)_dir.keyElementAt(i);
                    CharArrayQueue _queue = (CharArrayQueue)_dir.valueElementAt(i);
                    if (_key != null && _key.trim().length() > 0 && _queue != null) {
                        for (int j = 0; j < _queue.size(); j++) {
                            append(dirMapping, _key, _queue.peek(j));
                        }
                    }
                }
            }
        } else {
            System.out.println("SiteMapping#init() SystemManager.ini not found !!");
        }
        if (debugTable) debug();
        if (debug) System.out.println("△SiteMapping#init() end--------------------");
    }
    
    // マッピングデータを追加する data は他で参照、廃棄しないこと
    private void append(HashVector<CharArray,IntQueue> hv, CharArray key, int data) {
        if (hv != null && key != null && data > 0) {
            //if (debug) System.out.println("SiteMapping#append("+key+","+data+")");
            IntQueue queue = (IntQueue)hv.get(key);
            if (queue == null) {
                queue = new IntQueue();
                hv.put(new CharArray(key), queue);
            }
            boolean exist = false;  // 同データの存在をチェック
            for (int i = 0; i < queue.size(); i++) {
                if (queue.peek(i) == data) {
                    if (debug) System.out.println("SiteMapping#append("+key+","+data+") 同一データが存在します。"+(i+1)+"/"+queue.size());
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                //if (debug) System.out.println("  同一データが存在しないので追加します。");
                queue.enqueue(data);
            }
        } else {
            System.out.println("SiteMapping#append("+key+","+data+") error!");
        }
    }
    // マッピングデータを追加する data は他で参照、廃棄しないこと
    private void append(HashVector<CharArray,CharArrayQueue> hv, CharArray key, CharArray data) {
        if (hv != null && key != null && data != null) {
            //if (debug) System.out.println("SiteMapping#append("+key+","+data+")");
            CharArrayQueue queue = (CharArrayQueue)hv.get(key);
            if (queue == null) {
                queue = new CharArrayQueue();
                hv.put(new CharArray(key), queue);
            }
            boolean exist = false;  // 同データの存在をチェック
            for (int i = 0; i < queue.size(); i++) {
                if (queue.peek(i).equals(data)) {
                    if (debug) System.out.println("SiteMapping#append("+key+","+data+") 同一データが存在します。"+(i+1)+"/"+queue.size());
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                //if (debug) System.out.println("  同一データが存在しないので追加します。");
                queue.enqueue(new CharArray(data));
            }
        } else {
            System.out.println("SiteMapping#append("+key+","+data+") error!");
        }
    }

    // 逆マッピングデータを追加する data は他で参照、廃棄しないこと
    private void append(HashVector<IntObject,CharArrayQueue> hv, int key, CharArray data) {
        if (hv != null && key > 0 && data != null) {
            IntObject intObject = IntObject.pop(key);
            CharArrayQueue queue = (CharArrayQueue)hv.get(intObject);
            if (queue == null) {
                queue = new CharArrayQueue();
                hv.put(intObject, queue);
            } else {
                IntObject.push(intObject);
            }
            boolean exist = false;
            for (int i = 0; i < queue.size(); i++) {
                if (queue.peek(i).equals(data)) {
                    if (debug) System.out.println("SiteMapping#append("+key+","+data+") 同一データが存在します。"+(i+1)+"/"+queue.size());
                    exist = true;
                    break;
                }
            }
            if (!exist) queue.enqueue(new CharArray(data));
        } else {
            System.out.println("SiteMapping#append("+key+","+data+") error!");
        }
    }
    //private final String szAmp = "&amp;";
    private final String szAmp = "&";
    
    /** サイトパラメータURLを取得する */
    public CharArray getSiteParameter() { return getSiteParameter((CharArray)null); }
    /** サイトパラメータURLを取得する */
    public CharArray getSiteParameter(SessionObject session) { 
        return getSiteParameter(session, null); 
    }

    /**
        サイトパラメータを取得する (AnchorStringData#getAchorURL() で利用する)
        @param ch このバッファに追加する
    */
    public CharArray getSiteParameter(CharArray ch) {
        if (ch == null) ch = new CharArray();
        int site_code = mm.getSiteChannelCode();
if (debug) System.out.println("SiteMapping:getSiteParameter convert:"+SystemManager.convertContextPath+" site_code:"+site_code);
        if (SystemManager.convertContextPath == 1) {
            IntObject obj = IntObject.pop(site_code);
            CharArrayQueue queue = null;
            if (siteIDMapping != null) queue = (CharArrayQueue)siteIDMapping.get(obj);
            if (queue != null && queue.size() > 0) {
                ch.add(queue.peek());
            } else {
                if (ch.length() > 0 && ch.chars[ch.length()-1] != '/') ch.add('/');
                ch.add(SystemConst.siteKey); ch.add('=');
                ch.format(site_code);
            }
            IntObject.push(obj);
        } else {
            ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
            ch.add(SystemConst.siteKey); ch.add('=');
            ch.format(site_code);
        }
        return ch;
    }
    /**
        サイトパラメータを取得する (AnchorStringData#getAchorURL() で利用する)
        携帯端末でSiteKey が存在する場合はそちらを返す
        @param ch このバッファに追加する
    */
    public CharArray getSiteParameter(SessionObject session, CharArray ch) {
        if (ch == null) ch = new CharArray();
        do {
            if (session == null) break;
            String serverKey = mm.serverKey;
            int flg = mm.serverKeyFlg;
            if (serverKey == null || serverKey.length() == 0) break;
            if (flg < 1 || flg > 3) break;
            if (flg == 2 && session.isPC()) break; 
            if (flg == 3 && session.isPC() && session.hasCookie()) break;
            ch.add("/"+serverKey+"/");
            return ch;
        } while (false);
        getSiteParameter(ch);
        return ch;
    }

    public CharArray getSiteKey(SessionObject session) {
        return getSiteKey(session, null);
    }
    public CharArray getSiteKey(SessionObject session, CharArray ch) {
        if (ch == null) ch = new CharArray();
        do {
            if (session == null) break;
            String serverKey = mm.serverKey;
            int flg = mm.serverKeyFlg;
            if (serverKey == null || serverKey.length() == 0) break;
            if (flg < 1 || flg > 3) break;
            if (flg == 2 && session.isPC()) break; 
            if (flg == 3 && session.isPC() && session.hasCookie()) break;
            ch.add(serverKey);
            return ch;
        } while (false);
        ch.set(new CharArray(getSiteString()).replace("/",""));
        return ch;
    }


    /**
        サイトマッピング文字列を取得する
    */
    public CharArray getSiteString() {
        return getSiteString(mm.getSiteChannelCode());
    }
    /**
        サイトマッピング文字列を取得する
        取得文字列を加工しないこと！
    */
    public CharArray getSiteString(int site_channel_code) {
        CharArray ch = null;
        if (SystemManager.convertContextPath == 1) {
            IntObject obj = IntObject.pop(site_channel_code);
            CharArrayQueue queue = (CharArrayQueue)siteIDMapping.get(obj);
            IntObject.push(obj);
            if (queue != null) ch = queue.peek();
        }
        return ch;
    }

    /**
        ページパラメータを取得する (AnchorStringData#getAchorURL() で利用する)
        @param ch このバッファに追加する
        @param _id      module + page ID
    */
    public CharArray getPageParameter(CharArray ch, int _id) {
        if (ch == null) ch = new CharArray();
        if (SystemManager.convertContextPath == 1) {
            if (ch.length() > 0 && ch.chars[ch.length()-1] != '/') ch.add('/');
            IntObject obj = IntObject.pop(_id);
            CharArrayQueue queue = null;
            if (pageIDMapping != null) queue = (CharArrayQueue)pageIDMapping.get(obj);
            if (queue == null || queue.size() == 0) {
                if (pageIDMappingToContent != null) queue = (CharArrayQueue)pageIDMappingToContent.get(obj);
            }
            if (queue != null && queue.size()> 0) {
                ch.add(queue.peek());
            } else {
                CharArray _ch = getUserURL(_id);    // ユーザーマッピング処理追加
                if (_ch != null && _ch.length() > 0) ch.add(_ch);
                else {
                    ch.add(SystemConst.pageIDKey); ch.add('=');
                    ch.format(_id);
                }
            }
            IntObject.push(obj);
        } else {
            ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
            ch.add(SystemConst.pageIDKey); ch.add('=');
            ch.format(_id);
        }
        return ch;
    }

    /**
        ページIDに対応したPageMappingURLがあれば返す。
        @param _id      module + page ID
        @return         url 存在しない場合はNULL
    */
    public CharArray getPageURL(int _id) {
        CharArray rsts = null;
        if (SystemManager.convertContextPath == 1) {
            IntObject obj = IntObject.pop(_id);
            CharArrayQueue queue = null;
            if (pageIDMapping != null) queue = (CharArrayQueue)pageIDMapping.get(obj);
            if (queue != null && queue.size() > 0) {
                rsts = queue.peek();
                if (rsts.length() == 0) rsts = null;
            }
            IntObject.push(obj);
        }
        return rsts;
    }

    /**
        ページIDに対応したContentMappingURLがあれば返す。
        @param _id      module + page ID
        @return         url 存在しない場合はNULL
    */
    public CharArray getContentURL(int _id) {
        CharArray rsts = null;
        if (SystemManager.convertContextPath == 1) {
            IntObject obj = IntObject.pop(_id);
            CharArrayQueue queue = null;
            if (pageIDMappingToContent != null) queue = (CharArrayQueue)pageIDMappingToContent.get(obj);
            if (queue != null && queue.size() > 0) {
                rsts = queue.peek();
                if (rsts.length() == 0) rsts = null;
            }
            IntObject.push(obj);
        }
        return rsts;
    }

    /**
        ページIDに対応したUserPageMappingURLがあれば返す。
        @param _id      module + page ID
        @return         url 存在しない場合はNULL
    */
    public CharArray getUserURL(int _id) {
        CharArray rsts = null;
        if (SystemManager.convertContextPath == 1) {
            HashParameter hp = getUserPageMapping();
            rsts = hp.get(""+_id);
        }
        return rsts;
    }

    /**
       ページIDを取得する(AbstractServlet#getContextParametersで利用)
       @param  dir    ディレクトリ名
       @return PageID(1以上)   -1:存在しない
    */
    public int getPageID(CharArray dir) {
        int page_id = -1;
        if (pageMapping != null) {
            IntQueue queue = (IntQueue)pageMapping.get(dir);
            if (queue != null && queue.size() > 0) {
                page_id = queue.peek();
            }
        }
        return page_id;
    }
    public int getPageID(String dir) {
        CharArray ch = CharArray.pop(dir);
        int page_id = getPageID(ch);
        CharArray.push(ch);
        return page_id;
    }

    /**
       ディレクトリマッピング情報を取得する(AbstractServlet#getContextParametersで利用)
       @param  dir    ディレクトリ名
       @return マッピング情報 null:存在しない
    */
    public CharArray getDirectoryMap(CharArray dir) {
        CharArray ch = null;
        if (dirMapping != null) {
            CharArrayQueue queue = (CharArrayQueue)dirMapping.get(dir);
            if (queue != null && queue.size() > 0) {
                ch = queue.peek();
            }
        }
        return ch;
    }
    public CharArray getDirectoryMap(String dir) {
        CharArray ch = CharArray.pop(dir);
        CharArray ret = getDirectoryMap(ch);
        CharArray.push(ch);
        return ret;
    }
    
    private void debug() {
        System.out.println("●SiteMapping:"+(siteMapping != null));
        if (siteMapping != null) {
            for (int i = 0; i < siteMapping.size(); i++) {
                CharArray        key = (CharArray)siteMapping.keyElementAt(i);
                IntQueue        value = (IntQueue)siteMapping.valueElementAt(i);
                for (int j = 0; j < value.size(); j++) {
                    System.out.println("  "+key+"\t"+value.peek(j));
                }
            }
        }
        
        System.out.println("●PageMapping:"+(pageMapping != null));
        if (pageMapping != null) {
            for (int i = 0; i < pageMapping.size(); i++) {
                CharArray        key = (CharArray)pageMapping.keyElementAt(i);
                IntQueue value = (IntQueue)pageMapping.valueElementAt(i);
                for (int j = 0; j < value.size(); j++) {
                    System.out.println("  "+key+"\t"+value.peek(j));
                }
            }
        }
        
        System.out.println("●DirMapping:"+(dirMapping != null));
        if (dirMapping != null) {
            for (int i = 0; i < dirMapping.size(); i++) {
                CharArray        key = (CharArray)dirMapping.keyElementAt(i);
                CharArrayQueue value = (CharArrayQueue)dirMapping.valueElementAt(i);
                for (int j = 0; j < value.size(); j++) {
                    System.out.println("  "+key+"\t"+value.peek(j));
                }
            }
        }
        
        
        // 逆マッピングテーブル     HashVector:IntObject/CharArrayQueue(後優先）
        System.out.println("●SiteIDMapping:"+(siteIDMapping != null));
        if (siteIDMapping != null) {
            for (int i = 0; i < siteIDMapping.size(); i++) {
                IntObject        key = (IntObject)siteIDMapping.keyElementAt(i);
                CharArrayQueue value = (CharArrayQueue)siteIDMapping.valueElementAt(i);
                for (int j = 0; j < value.size(); j++) {
                    System.out.println("  "+key+"\t"+value.peek(j));
                }
            }
        }

        System.out.println("●PageIDMapping:"+(pageIDMapping != null));
        if (pageIDMapping != null) {
            for (int i = 0; i < pageIDMapping.size(); i++) {
                IntObject        key = (IntObject)pageIDMapping.keyElementAt(i);
                CharArrayQueue value = (CharArrayQueue)pageIDMapping.valueElementAt(i);
                for (int j = 0; j < value.size(); j++) {
                    System.out.println("  "+key+"\t"+value.peek(j));
                }
            }
        }
        System.out.println("●PageIDMappingToContent:"+(pageIDMappingToContent != null));
        if (pageIDMappingToContent != null) {
            for (int i = 0; i < pageIDMappingToContent.size(); i++) {
                IntObject        key = (IntObject)pageIDMappingToContent.keyElementAt(i);
                CharArrayQueue value = (CharArrayQueue)pageIDMappingToContent.valueElementAt(i);
                for (int j = 0; j < value.size(); j++) {
                    System.out.println("  "+key+"\t"+value.peek(j));
                }
            }
        }
    }

}

//
// [end of SiteMapping.java]
//


