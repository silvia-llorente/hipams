/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wm.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import wm.Security.RestSecurityFilter;
import java.io.InputStream;
import wm.WM_Crypt4GH;
import wm.WM_HCS;
import wm.WM_SS;

/**
 *
 * @author alumne
 */
public class UrlUtil {
    
    private static final String PATH = "/opt/HCService/storage";
    
    public static String getPATH(){
        return PATH;
    }
    
    public static void loadProps(){
        try {
            InputStream inputStream = UrlUtil.class.getClassLoader().getResourceAsStream("app.properties");
            Properties props= new Properties();
            props.load(inputStream);
            WM_HCS.setHCSUrl(props.getProperty("hcs.url"));
            WM_SS.setUrlSearch(props.getProperty("search.url"));
            WM_Crypt4GH.setUrlHCSC4GH(props.getProperty("hcsc4gh.url"));
            RestSecurityFilter.setProp(props.getProperty("oauth.url"));
//            AuthorizationUtil.setProp(props.getProperty("authorization.url"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
