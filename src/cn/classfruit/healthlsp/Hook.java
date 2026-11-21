package cn.classfruit.healthlsp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {

    private static final String TARGET = "com.heytap.health";
    private static final String TAG = "HealthLSP: ";

    private static volatile Set<String> sInstalledNames;
    private static volatile List<ResolveInfo> sSyntheticResolveInfos;
    private static volatile long sInstalledAt;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
        if (!TARGET.equals(lp.packageName)) {
            return;
        }
        ClassLoader cl = lp.classLoader;
        log("loaded in " + lp.processName);
        hookLauncherQuery(cl);
        hookInjectAppList(cl);
        hookIgnoreList(cl);
        hookForceAbandon(cl);
        hookSwitchStatus(cl);
        hookSearch(cl);
    }

    private void hookInjectAppList(final ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod("dg0.k", cl, "i", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object result = param.getResult();
                        if (!(result instanceof List)) {
                            return;
                        }
                        List<?> original = (List<?>) result;
                        Set<String> existing = new HashSet<>();
                        for (Object bean : original) {
                            Object pkg = XposedHelpers.callMethod(bean, "getPackageName");
                            if (pkg instanceof String) {
                                existing.add((String) pkg);
                            }
                        }
                        List<Object> merged = new ArrayList<>(original);
                        Class<?> beanClass = XposedHelpers.findClass(
                                "com.heytap.health.watch.notification.NotificationRoomBean", cl);
                        Set<String> all = installedNames(cl);
                        int added = 0;
                        for (String pkg : all) {
                            if (existing.contains(pkg)) {
                                continue;
                            }
                            merged.add(XposedHelpers.newInstance(beanClass, pkg, "", 0, "", false));
                            added++;
                        }
                        log("installed=" + all.size() + ", injected " + added);
                        param.setResult(merged);
                    } catch (Throwable t) {
                        log(t);
                    }
                }
            });
        } catch (Throwable t) {
            log("hook dg0.k.i failed");
            log(t);
        }
    }

    private void hookIgnoreList(final ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.heytap.health.watch.notification.impl.module.a", cl, "d",
                    String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                Object arg = param.args[0];
                                if (arg instanceof String && installedNames(cl).contains(arg)) {
                                    param.setResult(Boolean.FALSE);
                                }
                            } catch (Throwable t) {
                                log(t);
                            }
                        }
                    });
        } catch (Throwable t) {
            log("hook module.a.d failed");
            log(t);
        }
    }

    private void hookForceAbandon(final ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.heytap.health.watch.notification.impl.module.NotificationConfigBean",
                    cl, "getForceAbandon", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Object result = param.getResult();
                                if (!(result instanceof List)) {
                                    return;
                                }
                                Set<String> allow = installedNames(cl);
                                List<Object> filtered = new ArrayList<>();
                                for (Object item : (List<?>) result) {
                                    if (!(item instanceof String) || !allow.contains(item)) {
                                        filtered.add(item);
                                    }
                                }
                                if (filtered.size() != ((List<?>) result).size()) {
                                    param.setResult(filtered);
                                }
                            } catch (Throwable t) {
                                log(t);
                            }
                        }
                    });
        } catch (Throwable t) {
            log("hook NotificationConfigBean.getForceAbandon failed");
            log(t);
        }
    }

    private void hookSwitchStatus(final ClassLoader cl) {
        try {
            final Class<?> kClass = XposedHelpers.findClass("dg0.k", cl);
            XposedHelpers.findAndHookMethod("dg0.k", cl, "d", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (Boolean.TRUE.equals(param.getResult())) {
                            return;
                        }
                        Object arg = param.args[0];
                        if (!(arg instanceof String) || !installedNames(cl).contains(arg)) {
                            return;
                        }
                        Object bean = XposedHelpers.callStaticMethod(kClass, "h", arg);
                        if (bean != null && XposedHelpers.getBooleanField(bean, "isOpen")) {
                            param.setResult(Boolean.TRUE);
                        }
                    } catch (Throwable t) {
                        log(t);
                    }
                }
            });
        } catch (Throwable t) {
            log("hook dg0.k.d failed");
            log(t);
        }
    }

    private void hookLauncherQuery(final ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", cl,
                    "queryIntentActivities", Intent.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Object result = param.getResult();
                                Object intentArg = param.args[0];
                                if (!(result instanceof List) || !(intentArg instanceof Intent)) {
                                    return;
                                }
                                Intent intent = (Intent) intentArg;
                                if (!Intent.ACTION_MAIN.equals(intent.getAction())
                                        || !intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                                    return;
                                }
                                Set<String> have = new HashSet<>();
                                for (Object item : (List<?>) result) {
                                    if (!(item instanceof ResolveInfo)) {
                                        continue;
                                    }
                                    ResolveInfo ri = (ResolveInfo) item;
                                    if (ri.activityInfo != null
                                            && ri.activityInfo.packageName != null) {
                                        have.add(ri.activityInfo.packageName);
                                    }
                                }
                                List<Object> merged = new ArrayList<>((List<?>) result);
                                int added = 0;
                                for (ResolveInfo ri : syntheticResolveInfos(cl)) {
                                    if (ri.activityInfo == null
                                            || have.contains(ri.activityInfo.packageName)) {
                                        continue;
                                    }
                                    merged.add(ri);
                                    added++;
                                }
                                if (added > 0) {
                                    log("launcher query augmented +" + added);
                                    param.setResult(merged);
                                }
                            } catch (Throwable t) {
                                log(t);
                            }
                        }
                    });
            log("hooked ApplicationPackageManager.queryIntentActivities");
        } catch (Throwable t) {
            log("hook queryIntentActivities failed");
            log(t);
        }
    }

    private static Set<String> installedNames(ClassLoader cl) {
        ensureInstalled(cl);
        Set<String> names = sInstalledNames;
        return names != null ? names : java.util.Collections.<String>emptySet();
    }

    private static List<ResolveInfo> syntheticResolveInfos(ClassLoader cl) {
        ensureInstalled(cl);
        List<ResolveInfo> list = sSyntheticResolveInfos;
        return list != null ? list : java.util.Collections.<ResolveInfo>emptyList();
    }

    private static void ensureInstalled(ClassLoader cl) {
        long now = System.currentTimeMillis();
        if (sInstalledNames != null && now - sInstalledAt < 60_000L) {
            return;
        }
        try {
            Context ctx = appContext(cl);
            if (ctx == null) {
                return;
            }
            PackageManager pm = ctx.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);
            if (apps == null || apps.isEmpty()) {
                return;
            }
            Set<String> names = new HashSet<>();
            List<ResolveInfo> synthetic = new ArrayList<>();
            for (ApplicationInfo ai : apps) {
                if (ai == null || ai.packageName == null) {
                    continue;
                }
                names.add(ai.packageName);
                ActivityInfo activity = new ActivityInfo();
                activity.packageName = ai.packageName;
                activity.name = ai.packageName + ".Launcher";
                activity.applicationInfo = ai;
                ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = activity;
                synthetic.add(ri);
            }
            if (names.isEmpty()) {
                return;
            }
            sInstalledNames = names;
            sSyntheticResolveInfos = synthetic;
            sInstalledAt = now;
        } catch (Throwable t) {
            log(t);
        }
    }

    private static Context appContext(ClassLoader cl) {
        try {
            android.app.Application app = android.app.AndroidAppHelper.currentApplication();
            if (app != null) {
                return app;
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> m = XposedHelpers.findClass("com.heytap.health.base.base.m", cl);
            Object ctx = XposedHelpers.getStaticObjectField(m, "a");
            if (ctx instanceof Context) {
                return (Context) ctx;
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> at = XposedHelpers.findClass("android.app.ActivityThread", cl);
            Object app = XposedHelpers.callStaticMethod(at, "currentApplication");
            if (app instanceof Context) {
                return (Context) app;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static volatile String sLastSearchQuery = "";

    private void hookSearch(final ClassLoader cl) {
        try {
            final String frag = "com.heytap.health.watch.notification.impl.ui.NotificationSyncFragment";
            Class<?> screenClass = XposedHelpers.findClass("androidx.preference.PreferenceScreen", cl);
            XC_MethodHook addHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object screen = null;
                        if (param.args.length > 0 && param.args[0] != null
                                && param.args[0].getClass().getName()
                                .endsWith("PreferenceScreen")) {
                            screen = param.args[0];
                        } else if (param.thisObject != null) {
                            screen = XposedHelpers.callMethod(param.thisObject,
                                    "getPreferenceScreen");
                        }
                        if (screen != null) {
                            addSearchPreference(cl, param.thisObject, screen);
                        }
                    } catch (Throwable t) {
                        log(t);
                    }
                }
            };
            XposedHelpers.findAndHookMethod(frag, cl, "onResume", addHook);
            XposedHelpers.findAndHookMethod(frag, cl, "f0", screenClass, addHook);
            log("search hooks installed");
        } catch (Throwable t) {
            log("hook search failed");
            log(t);
        }
    }

    private static void addSearchPreference(final ClassLoader cl, final Object fragment,
            final Object screen) {
        try {
            Object existing = XposedHelpers.callMethod(screen, "findPreference", "healthlsp_search");
            if (existing != null) {
                applySearchFilter(cl, screen, sLastSearchQuery);
                return;
            }
            Object ctx = XposedHelpers.callMethod(screen, "getContext");
            Class<?> editClass = XposedHelpers.findClass("androidx.preference.EditTextPreference", cl);
            final Object pref = XposedHelpers.newInstance(editClass, ctx);
            XposedHelpers.callMethod(pref, "setKey", "healthlsp_search");
            XposedHelpers.callMethod(pref, "setTitle", "搜索应用");
            XposedHelpers.callMethod(pref, "setSummary", "输入应用名或包名筛选");
            XposedHelpers.callMethod(pref, "setDialogTitle", "搜索应用");
            try {
                XposedHelpers.callMethod(pref, "setOrder", -10000);
            } catch (Throwable ignored) {
            }
            Class<?> listenerClass = XposedHelpers.findClass(
                    "androidx.preference.Preference$OnPreferenceChangeListener", cl);
            Object listener = java.lang.reflect.Proxy.newProxyInstance(cl,
                    new Class<?>[]{listenerClass},
                    new java.lang.reflect.InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
                            if ("onPreferenceChange".equals(method.getName())) {
                                Object value = args != null && args.length > 1 ? args[1] : null;
                                String query = value == null ? "" : value.toString();
                                sLastSearchQuery = query;
                                applySearchFilter(cl, screen, query);
                                try {
                                    XposedHelpers.callMethod(pref, "setSummary",
                                            query.trim().isEmpty()
                                                    ? "输入应用名或包名筛选"
                                                    : "筛选：" + query);
                                } catch (Throwable ignored) {
                                }
                                return Boolean.TRUE;
                            }
                            return null;
                        }
                    });
            XposedHelpers.callMethod(pref, "setOnPreferenceChangeListener", listener);
            XposedHelpers.callMethod(screen, "addPreference", pref);
            try {
                Object list = XposedHelpers.getObjectField(screen, "mPreferenceList");
                if (list instanceof List) {
                    ((List<?>) list).remove(pref);
                    ((List) list).add(0, pref);
                }
                XposedHelpers.callMethod(screen, "notifyChanged");
            } catch (Throwable ignored) {
            }
            try {
                Object listView = XposedHelpers.callMethod(fragment, "getListView");
                Object adapter = XposedHelpers.callMethod(listView, "getAdapter");
                if (adapter != null) {
                    XposedHelpers.callMethod(adapter, "notifyDataSetChanged");
                }
            } catch (Throwable ignored) {
            }
            applySearchFilter(cl, screen, sLastSearchQuery);
            log("search preference added, order=" + XposedHelpers.callMethod(pref, "getOrder"));
        } catch (Throwable t) {
            log("add search preference failed");
            log(t);
        }
    }

    private static void applySearchFilter(ClassLoader cl, Object group, String query) {
        try {
            String q = query == null ? "" : query.trim().toLowerCase();
            filterGroup(cl, group, q);
        } catch (Throwable t) {
            log(t);
        }
    }

    private static boolean filterGroup(ClassLoader cl, Object group, String query) {
        boolean anyVisible = false;
        int count = (Integer) XposedHelpers.callMethod(group, "getPreferenceCount");
        Class<?> groupClass = XposedHelpers.findClass("androidx.preference.PreferenceGroup", cl);
        for (int i = 0; i < count; i++) {
            Object p = XposedHelpers.callMethod(group, "getPreference", i);
            String key = stringOf(XposedHelpers.callMethod(p, "getKey"));
            if ("healthlsp_search".equals(key)) {
                anyVisible = true;
                continue;
            }
            if (groupClass.isInstance(p)) {
                boolean childVisible = filterGroup(cl, p, query);
                if (!query.isEmpty()) {
                    XposedHelpers.callMethod(p, "setVisible", childVisible);
                } else {
                    XposedHelpers.callMethod(p, "setVisible", true);
                }
                anyVisible |= childVisible;
            } else {
                String title = stringOf(XposedHelpers.callMethod(p, "getTitle"));
                String summary = stringOf(XposedHelpers.callMethod(p, "getSummary"));
                boolean match = query.isEmpty()
                        || title.toLowerCase().contains(query)
                        || summary.toLowerCase().contains(query)
                        || key.toLowerCase().contains(query);
                XposedHelpers.callMethod(p, "setVisible", match);
                anyVisible |= match;
            }
        }
        return anyVisible;
    }

    private static String stringOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private static void log(String msg) {
        XposedBridge.log(TAG + msg);
    }

    private static void log(Throwable t) {
        XposedBridge.log(TAG + t);
    }
}
