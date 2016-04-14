package edu.ohio_state.org.vetplus.foalscore.util;


    import android.content.Context;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;

    import java.net.InetAddress;
    import java.net.UnknownHostException;

/**
 * Created by Veena on 4/6/2015.
 */


    public class ConnectionDetector {

        private Context _context;

        public ConnectionDetector(Context context){
            this._context = context;
        }

        public boolean isConnectingToInternet(){
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean connectedToInternet = false;
            if (connectivity != null)
            {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for(int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            connectedToInternet = true;
                        }
                    }
                    if(connectedToInternet) {
                        if(testDNS("foalscore.org.ohio-state.edu")) {
                            return true;
                        }
                    }
                }
            } else {
                return false;
            }
            return false;
        }

    private static boolean testDNS(String hostname) {
        try
        {
            DNSResolver dnsRes = new DNSResolver(hostname);
            Thread t = new Thread(dnsRes);
            t.start();
            t.join(1000);
            InetAddress inetAddr = dnsRes.get();
            return inetAddr != null;
        } catch(Exception e) {
            return false;
        }
    }

    private static class DNSResolver implements Runnable {
        private String domain;
        private InetAddress inetAddr;

        public DNSResolver(String domain) {
            this.domain = domain;
        }

        public void run() {
            try {
                InetAddress addr = InetAddress.getByName(domain);
                set(addr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        public synchronized void set(InetAddress inetAddr) {
            this.inetAddr = inetAddr;
        }
        public synchronized InetAddress get() {
            return inetAddr;
        }
    }

}

