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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

public class AboutActivity extends Activity {

    private static final String AVATAR_URL = "https://github.com/fruit0730.png?size=240";
    private static final String HOME_URL = "https://classfruit.cn/";
    private static final String PROJECTS_URL = "https://classfruit.cn/#projects";
    private static final String BLOG_URL = "https://classfruit.cn/#blog";
    private static final String CONTACT_URL = "https://classfruit.cn/#contact";
    private static final String GITHUB_URL = "https://github.com/fruit0730";
    private static final String ACTIVITY_URL = "https://github.com/fruit0730?tab=activity";
    private static final String TOOLBOX_URL = "https://toolbox.classfruit.cn";
    private static final String BILIBILI_URL = "https://space.bilibili.com/3494376982448554";
    private static final String GITEE_URL = "https://gitee.com/pipa3288582557";
    private static final String REPO_URL = "https://github.com/fruit0730/HealthLSP";

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
        buildModule();
        buildLinks();
        buildFooter();
        loadAvatar();
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_HORIZONTAL);

        avatarSize = dp(96);
        avatarView = new ImageView(this);
        avatarView.setLayoutParams(new LinearLayout.LayoutParams(avatarSize, avatarSize));
        avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarView.setImageBitmap(makeAvatarPlaceholder(avatarSize));
        header.addView(avatarView);

        TextView name = text("ClassFruit", 30, TEXT, Typeface.BOLD);
        name.setPadding(0, dp(16), 0, 0);
        header.addView(name);

        TextView link = text("开发者主页  →", 13, PRIMARY, Typeface.BOLD);
        link.setPadding(0, dp(8), 0, 0);
        link.setOnClickListener(openListener(HOME_URL));
        header.addView(link);

        content.addView(header);
    }

    private void buildModule() {
        section("关于 HealthLSP", "LSPosed · OPPO 健康通知同步列表增强");
        LinearLayout card = card();
        String version = "1.3.1";
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
        repo.setOnClickListener(openListener(REPO_URL));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        card.addView(repo, lp);
        content.addView(card);
    }

    private void buildLinks() {
        section("跳转链接", "个人资料、项目与动态请前往网站查看");
        LinearLayout card = linksCard();
        addLink(card, "个人主页", "classfruit.cn", HOME_URL, false);
        addLink(card, "我的项目", "classfruit.cn/#projects", PROJECTS_URL, true);
        addLink(card, "最新动态", "classfruit.cn/#blog", BLOG_URL, true);
        addLink(card, "联系我", "classfruit.cn/#contact", CONTACT_URL, true);
        addLink(card, "GitHub", "github.com/fruit0730", GITHUB_URL, true);
        addLink(card, "GitHub 活动", "github.com/fruit0730", ACTIVITY_URL, true);
        addLink(card, "工具箱", "toolbox.classfruit.cn", TOOLBOX_URL, true);
        addLink(card, "Bilibili", "space.bilibili.com", BILIBILI_URL, true);
        addLink(card, "Gitee", "gitee.com/pipa3288582557", GITEE_URL, true);
        content.addView(card);
    }

    private void addLink(LinearLayout card, String title, String subtitle, final String url,
            boolean divider) {
        if (divider) {
            View line = new View(this);
            line.setBackgroundColor(BORDER);
            LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
            lineLp.leftMargin = dp(16);
            lineLp.rightMargin = dp(16);
            card.addView(line, lineLp);
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(13), dp(16), dp(13));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView titleView = text(title, 14, TEXT, Typeface.NORMAL);
        TextView subtitleView = text(subtitle, 11, TERTIARY, Typeface.NORMAL);
        subtitleView.setPadding(0, dp(3), 0, 0);
        texts.addView(titleView);
        texts.addView(subtitleView);
        row.addView(texts);

        TextView arrow = text("↗", 16, PRIMARY, Typeface.BOLD);
        row.addView(arrow);
        row.setOnClickListener(openListener(url));
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void buildFooter() {
        TextView copyright = text("© 2026 ClassFruit · MIT License", 11, TERTIARY,
                Typeface.NORMAL);
        copyright.setGravity(Gravity.CENTER);
        copyright.setPadding(0, dp(22), 0, 0);
        content.addView(copyright);
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
        card.setBackground(cardBackground());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout linksCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(cardBackground());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        return card;
    }

    private GradientDrawable cardBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), BORDER);
        return bg;
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

    private Bitmap fetchBitmap(String address) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "HealthLSP/1.3.1");
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
