package cn.classfruit.healthlsp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AboutActivity extends Activity {

    private static final String AVATAR_URL = "https://github.com/fruit0730.png?size=240";
    private static final String HOME_URL = "https://classfruit.cn/";
    private static final String GITHUB_URL = "https://github.com/fruit0730";
    private static final String TOOLBOX_URL = "https://toolbox.classfruit.cn";
    private static final String BILIBILI_URL = "https://space.bilibili.com/3494376982448554";
    private static final String GITEE_URL = "https://gitee.com/pipa3288582557";
    private static final String BLOG_URL = "https://classfruit.cn/blog/FineKeeperDisclaimer.html";
    private static final String EVENTS_URL =
            "https://api.github.com/users/fruit0730/events/public?per_page=5";

    private static final int BG = 0xFF0A0A0F;
    private static final int CARD = 0xFF16161F;
    private static final int CARD_ALT = 0xFF1E1E28;
    private static final int TEXT = 0xFFF5F5F7;
    private static final int SECONDARY = 0xFF98989D;
    private static final int TERTIARY = 0xFF6E6E73;
    private static final int BORDER = 0x14FFFFFF;
    private static final int PRIMARY = 0xFF007AFF;
    private static final int ACCENT = 0xFF5856D6;

    private final Handler main = new Handler(Looper.getMainLooper());
    private LinearLayout content;
    private ImageView avatarView;
    private int avatarSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.getDecorView().setSystemUiVisibility(0);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        content.setPadding(pad, dp(28), pad, dp(30));
        scroll.addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        buildHeader();
        buildProjects();
        buildBlog();
        buildActivity();
        buildModule();
        buildFooter();
        loadAvatar();
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setPadding(0, dp(6), 0, dp(6));

        avatarSize = dp(96);
        avatarView = new ImageView(this);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        avatarView.setLayoutParams(avatarLp);
        avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarView.setImageBitmap(makeAvatarPlaceholder(avatarSize));
        header.addView(avatarView);

        TextView name = text("ClassFruit", 30, TEXT, Typeface.BOLD);
        name.setPadding(0, dp(16), 0, 0);
        header.addView(name);

        TextView handle = text("饶林果 · @fruit0730", 13, SECONDARY, Typeface.NORMAL);
        handle.setPadding(0, dp(4), 0, 0);
        header.addView(handle);

        TextView badge = chip("学生开发者", PRIMARY);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.topMargin = dp(12);
        header.addView(badge, badgeLp);

        TextView bio = text("一个热爱技术的高中生，喜欢探索编程的无限可能性。", 14, SECONDARY,
                Typeface.NORMAL);
        bio.setGravity(Gravity.CENTER);
        bio.setLineSpacing(dp(4), 1f);
        bio.setPadding(dp(10), dp(16), dp(10), dp(4));
        header.addView(bio);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, dp(18), 0, dp(8));
        addStat(stats, "3", "项目");
        addStat(stats, "2", "博客");
        addStat(stats, "2021", "始于");
        header.addView(stats);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(12), 0, 0);
        TextView home = actionButton("个人主页", PRIMARY, Color.WHITE);
        home.setOnClickListener(openListener(HOME_URL));
        TextView github = actionButton("GitHub", CARD_ALT, TEXT);
        github.setOnClickListener(openListener(GITHUB_URL));
        TextView toolbox = actionButton("工具箱", CARD_ALT, TEXT);
        toolbox.setOnClickListener(openListener(TOOLBOX_URL));
        addAction(actions, home);
        addAction(actions, github);
        addAction(actions, toolbox);
        header.addView(actions);

        content.addView(header);
    }

    private void addStat(LinearLayout parent, String number, String label) {
        LinearLayout stat = new LinearLayout(this);
        stat.setOrientation(LinearLayout.VERTICAL);
        stat.setGravity(Gravity.CENTER);
        stat.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView n = text(number, 20, TEXT, Typeface.BOLD);
        TextView l = text(label, 11, SECONDARY, Typeface.NORMAL);
        l.setPadding(0, dp(2), 0, 0);
        stat.addView(n);
        stat.addView(l);
        parent.addView(stat);
    }

    private void addAction(LinearLayout parent, TextView button) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.leftMargin = dp(4);
        lp.rightMargin = dp(4);
        parent.addView(button, lp);
    }

    private void buildProjects() {
        section("我的项目", "Projects · 探索我创造的一些有趣的项目");

        projectCard("R-工具箱", "多功能开发工具集合，提供日常开发中常用的工具和实用功能。",
                "Web应用 · 工具集", "立即体验", TOOLBOX_URL);
        projectCard("ReadLiner", "Python 模块，可以高效读取自定义文件行数，支持多种文件格式。",
                "Python · 文件处理", "查看源码", "https://github.com/fruit0730/ReadLiner");
        projectCard("QIMG", "简单易用的 Python 工具，提供获取和下载 QQ 用户头像的 API 接口。",
                "Python · API", "查看源码", "https://github.com/fruit0730/QIMG/tree/master");
    }

    private void projectCard(String title, String desc, String tags, String linkText,
            final String url) {
        LinearLayout card = card();
        TextView titleView = text(title, 17, TEXT, Typeface.BOLD);
        card.addView(titleView);
        TextView descView = text(desc, 13, SECONDARY, Typeface.NORMAL);
        descView.setLineSpacing(dp(3), 1f);
        descView.setPadding(0, dp(6), 0, 0);
        card.addView(descView);
        TextView tagView = text(tags, 11, PRIMARY, Typeface.NORMAL);
        tagView.setPadding(0, dp(10), 0, 0);
        card.addView(tagView);
        TextView link = text(linkText + "  →", 13, PRIMARY, Typeface.BOLD);
        link.setPadding(0, dp(12), 0, 0);
        card.addView(link);
        card.setOnClickListener(openListener(url));
        content.addView(card);
    }

    private void buildBlog() {
        section("最新动态", "Blog · 分享技术见解和项目更新");
        LinearLayout card = card();
        TextView category = chip("公告", ACCENT);
        card.addView(category);
        TextView date = text("2025年4月12日", 11, TERTIARY, Typeface.NORMAL);
        date.setPadding(0, dp(10), 0, 0);
        card.addView(date);
        TextView title = text("发客(FineKeeper)工作室资源站免责声明", 16, TEXT, Typeface.BOLD);
        title.setPadding(0, dp(6), 0, 0);
        card.addView(title);
        TextView desc = text("本文详细说明了发客工作室资源站的使用条款和免责声明。", 13, SECONDARY,
                Typeface.NORMAL);
        desc.setPadding(0, dp(6), 0, 0);
        card.addView(desc);
        TextView read = text("阅读全文  →", 13, PRIMARY, Typeface.BOLD);
        read.setPadding(0, dp(12), 0, 0);
        card.addView(read);
        card.setOnClickListener(openListener(BLOG_URL));
        content.addView(card);
    }

    private void buildActivity() {
        section("GitHub 活动", "Activity · 最近在 GitHub 上的动态");
        final LinearLayout card = card();
        final TextView status = text("正在加载 GitHub 活动…", 13, SECONDARY, Typeface.NORMAL);
        card.addView(status);
        content.addView(card);

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> events = new ArrayList<>();
                String error = null;
                try {
                    String json = httpGet(EVENTS_URL);
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length() && events.size() < 5; i++) {
                        JSONObject event = array.optJSONObject(i);
                        if (event != null) {
                            events.add(eventText(event));
                        }
                    }
                } catch (Throwable t) {
                    error = t.getMessage();
                }
                final List<String> result = events;
                final String err = error;
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        card.removeAllViews();
                        if (!result.isEmpty()) {
                            for (String line : result) {
                                SpannableStringBuilder sb = new SpannableStringBuilder("●  ");
                                sb.setSpan(new ForegroundColorSpan(PRIMARY), 0, 1, 0);
                                sb.append(line);
                                TextView row = text(sb, 13, SECONDARY, Typeface.NORMAL);
                                row.setLineSpacing(dp(3), 1f);
                                row.setPadding(0, dp(7), 0, dp(7));
                                card.addView(row);
                            }
                        } else {
                            String msg = err == null ? "暂无公开活动" : "加载失败，点击打开 GitHub";
                            TextView row = text(msg, 13, SECONDARY, Typeface.NORMAL);
                            row.setPadding(0, dp(4), 0, dp(4));
                            row.setOnClickListener(openListener(GITHUB_URL));
                            card.addView(row);
                        }
                    }
                });
            }
        }).start();
    }

    private void buildModule() {
        section("关于 HealthLSP", "LSPosed · OPPO 健康通知同步列表增强");
        LinearLayout card = card();
        String version = "1.3";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Throwable ignored) {
        }
        TextView title = text("HealthLSP v" + version, 17, TEXT, Typeface.BOLD);
        card.addView(title);
        TextView pkg = text("cn.classfruit.healthlsp", 11, TERTIARY, Typeface.NORMAL);
        pkg.setPadding(0, dp(4), 0, 0);
        card.addView(pkg);
        TextView desc = text("让 OPPO 健康的“同步手机通知”列表显示全部已安装应用（含无图标系统包），"
                + "并支持搜索和系统应用放行。", 13, SECONDARY, Typeface.NORMAL);
        desc.setLineSpacing(dp(3), 1f);
        desc.setPadding(0, dp(10), 0, 0);
        card.addView(desc);
        TextView hint = text("使用：LSPosed 启用模块 → 作用域勾选 OPPO 健康 → 重开应用", 12,
                PRIMARY, Typeface.NORMAL);
        hint.setPadding(0, dp(10), 0, 0);
        card.addView(hint);
        TextView repo = actionButton("打开 GitHub 仓库", CARD_ALT, TEXT);
        repo.setOnClickListener(openListener("https://github.com/fruit0730/HealthLSP"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        card.addView(repo, lp);
        content.addView(card);
    }

    private void buildFooter() {
        LinearLayout links = new LinearLayout(this);
        links.setOrientation(LinearLayout.HORIZONTAL);
        links.setGravity(Gravity.CENTER);
        links.setPadding(0, dp(24), 0, dp(10));
        links.addView(footerLink("Bilibili", BILIBILI_URL));
        links.addView(footerLink("Gitee", GITEE_URL));
        links.addView(footerLink("个人主页", HOME_URL));
        content.addView(links);

        TextView copyright = text("© 2026 ClassFruit · MIT License", 11, TERTIARY,
                Typeface.NORMAL);
        copyright.setGravity(Gravity.CENTER);
        content.addView(copyright);
    }

    private TextView footerLink(String label, final String url) {
        TextView link = text(label, 12, SECONDARY, Typeface.NORMAL);
        link.setPadding(dp(8), 0, dp(8), 0);
        link.setOnClickListener(openListener(url));
        return link;
    }

    private void section(String title, String subtitle) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, dp(30), 0, dp(10));
        TextView t = text(title, 18, TEXT, Typeface.BOLD);
        header.addView(t);
        TextView s = text(subtitle, 11, TERTIARY, Typeface.NORMAL);
        s.setPadding(0, dp(4), 0, 0);
        header.addView(s);
        content.addView(header);
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), BORDER);
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        return card;
    }

    private TextView chip(String label, int color) {
        TextView chip = text(label, 11, color, Typeface.BOLD);
        chip.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor((color & 0x00FFFFFF) | 0x22000000);
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), (color & 0x00FFFFFF) | 0x33000000);
        chip.setBackground(bg);
        return chip;
    }

    private TextView actionButton(String label, int background, int textColor) {
        TextView button = text(label, 13, textColor, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), dp(11), dp(12), dp(11));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(background);
        bg.setCornerRadius(dp(12));
        if (background == CARD_ALT) {
            bg.setStroke(dp(1), BORDER);
        }
        button.setBackground(bg);
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private TextView text(CharSequence value, float size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", style));
        view.setIncludeFontPadding(false);
        return view;
    }

    private void loadAvatar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap source = fetchBitmap(AVATAR_URL);
                if (source == null) {
                    return;
                }
                final Bitmap circle = circleCrop(source, avatarSize);
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        if (avatarView != null && circle != null) {
                            avatarView.setImageBitmap(circle);
                        }
                    }
                });
            }
        }).start();
    }

    private Bitmap makeAvatarPlaceholder(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, size, size, PRIMARY, ACCENT,
                Shader.TileMode.CLAMP));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.34f);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("CF", size / 2f, size / 2f + textPaint.getTextSize() / 3f, textPaint);
        return bitmap;
    }

    private Bitmap circleCrop(Bitmap source, int size) {
        try {
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP));
            float scale = Math.max((float) size / source.getWidth(),
                    (float) size / source.getHeight());
            canvas.scale(scale, scale);
            canvas.drawCircle(source.getWidth() / 2f, source.getHeight() / 2f,
                    Math.min(source.getWidth(), source.getHeight()) / 2f, paint);
            return output;
        } catch (Throwable t) {
            return null;
        }
    }

    private String eventText(JSONObject event) {
        String type = event.optString("type");
        String repo = "";
        JSONObject repoObj = event.optJSONObject("repo");
        if (repoObj != null) {
            repo = repoObj.optString("name");
        }
        JSONObject payload = event.optJSONObject("payload");
        String action;
        switch (type) {
            case "PushEvent": {
                int count = 1;
                if (payload != null) {
                    JSONArray commits = payload.optJSONArray("commits");
                    if (commits != null && commits.length() > 0) {
                        count = commits.length();
                    }
                }
                action = "推送了 " + count + " 个提交";
                break;
            }
            case "CreateEvent": {
                String ref = payload == null ? "" : payload.optString("ref_type");
                action = "repository".equals(ref) ? "创建了仓库" : "创建了 " + ref;
                break;
            }
            case "WatchEvent":
                action = "Star 了仓库";
                break;
            case "ForkEvent":
                action = "Fork 了仓库";
                break;
            case "IssuesEvent":
                action = "更新了 Issue";
                break;
            case "PullRequestEvent":
                action = "更新了 Pull Request";
                break;
            case "ReleaseEvent":
                action = "发布了 Release";
                break;
            case "PublicEvent":
                action = "开源了仓库";
                break;
            default:
                action = type;
                break;
        }
        String time = relativeTime(event.optString("created_at"));
        if (repo.isEmpty()) {
            return action + " · " + time;
        }
        return action + " · " + repo + " · " + time;
    }

    private String relativeTime(String iso) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = format.parse(iso);
            if (date == null) {
                return "";
            }
            long diff = (System.currentTimeMillis() - date.getTime()) / 1000L;
            if (diff < 60) {
                return "刚刚";
            }
            if (diff < 3600) {
                return (diff / 60) + " 分钟前";
            }
            if (diff < 86400) {
                return (diff / 3600) + " 小时前";
            }
            if (diff < 604800) {
                return (diff / 86400) + " 天前";
            }
            return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(date);
        } catch (Throwable t) {
            return "";
        }
    }

    private String httpGet(String address) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "HealthLSP/1.2");
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            int code = connection.getResponseCode();
            InputStream input = code >= 200 && code < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            if (input == null) {
                throw new IllegalStateException("HTTP " + code);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("HTTP " + code);
            }
            return builder.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Bitmap fetchBitmap(String address) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "HealthLSP/1.2");
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                return null;
            }
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (Throwable t) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private View.OnClickListener openListener(final String url) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open(url);
            }
        };
    }

    private void open(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
