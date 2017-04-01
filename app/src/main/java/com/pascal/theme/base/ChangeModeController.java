package com.pascal.theme.base;;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.pascal.changemodeSimple.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class ChangeModeController {

    private static final String ATTR_BACKGROUND = "backgroundAttr";
    private static final String ATTR_BACKGROUND_DRAWABLE = "backgroundDrawableAttr";
    private static final String ATTR_TEXTCOLOR = "textColorAttr";

    private static List<AttrEntity<View>> mBackGroundViews;
    private static List<AttrEntity<View>> mBackGroundDrawableViews;
    private static List<AttrEntity<TextView>> mTextColorViews;

    private static ChangeModeController mChangeModeController;

    private ChangeModeController(){}
    public static ChangeModeController getInstance(){
        if(mChangeModeController == null){
            mChangeModeController = new ChangeModeController();
        }
        return mChangeModeController;
    }

    private void init(){
        mBackGroundViews = new ArrayList<>();
        mTextColorViews = new ArrayList<>();
        mBackGroundDrawableViews = new ArrayList<>();
    }

    public ChangeModeController init(final Activity activity, final Class mClass){
        init();
        LayoutInflaterCompat.setFactory(LayoutInflater.from(activity), new LayoutInflaterFactory() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                View view = null;
                try {
                    if(name.indexOf('.') == -1){
                        if ("View".equals(name)) {
                            view = LayoutInflater.from(context).createView(name, "android.view.", attrs);
                        }
                        if (view == null) {
                            view = LayoutInflater.from(context).createView(name, "android.widget.", attrs);
                        }
                        if (view == null) {
                            view = LayoutInflater.from(context).createView(name, "android.webkit.", attrs);
                        }

                    }else{
                        if (view == null){
                            view = LayoutInflater.from(context).createView(name, null, attrs);
                        }
                    }
                    if(view != null){
                        for (int i = 0; i < attrs.getAttributeCount(); i++) {
                            if (attrs.getAttributeName(i).equals(ATTR_BACKGROUND)) {
                                mBackGroundViews.add(new AttrEntity<View>(view,getAttr(mClass,attrs.getAttributeValue(i))));
                            }
                            if (attrs.getAttributeName(i).equals(ATTR_TEXTCOLOR)) {
                                mTextColorViews.add(new AttrEntity<TextView>((TextView)view,getAttr(mClass,attrs.getAttributeValue(i))));
                            }
                            if (attrs.getAttributeName(i).equals(ATTR_BACKGROUND_DRAWABLE)) {
                                mBackGroundDrawableViews.add(new AttrEntity<View>(view,getAttr(mClass,attrs.getAttributeValue(i))));
                            }

                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return view;
            }
        });
        return this;
    }

    /**
     * 反射获取文件id
     * @param attrName 属性名称
     * @return  属性id
     */
    public static int getAttr(Class draw, String attrName) {
        if (attrName == null || attrName.trim().equals("") || draw == null) {
            return R.attr.colorPrimary;
        }
        try {
            Field field = draw.getDeclaredField(attrName);
            //field.setAccessible(true);
            return field.getInt(attrName);
        } catch (Exception e) {
            return R.attr.colorPrimary;
        }
    }

    /**
     * 设置当前主题
     * @param ctx  上下文
     * 从缓存里面取值.没有是返回第0个
     *
     */
    public static void setCurrentTheme(Context ctx){
        ctx.setTheme(ChangeModeHelper.MODE_RES_ID[ChangeModeHelper.getChangeMode(ctx)]);
    }
    /**
     *
     * @param ctx 上下文
     * @param resId 为ChangeModeHelper.MODE_INDEX_ENUM值 .
     *              eg:ChangeModeHelper.MODE_INDEX_ENUM.MODE_DAY.getIndex()
     */
    public static void changeTheme(Activity ctx, int resId) {
        if(mBackGroundDrawableViews == null || mTextColorViews == null || mBackGroundViews == null){
            throw new RuntimeException("请先调用init()初始化方法!");
        }
        ChangeModeHelper.setChangeMode(ctx, resId);
        ctx.setTheme(ChangeModeHelper.MODE_RES_ID[resId]);
        showAnimation(ctx);
        refreshUI(ctx);
    }


    /**
     * 刷新UI界面
     * @param ctx  上下文
     */
    private static void refreshUI(Activity ctx) {

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = ctx.getTheme();

        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        View view = ctx.findViewById(R.id.action_bar);
        if(view!=null){
            view.setBackgroundResource(typedValue.resourceId);
        }

        for(AttrEntity<View> entity:mBackGroundViews){
            theme.resolveAttribute(entity.colorId, typedValue, true);
            entity.v.setBackgroundResource(typedValue.resourceId);
        }

        for(AttrEntity<View> entity:mBackGroundDrawableViews){
            theme.resolveAttribute(entity.colorId, typedValue, true);
            entity.v.setBackgroundResource(typedValue.resourceId);
        }

        for (AttrEntity<TextView> entity:mTextColorViews){
            theme.resolveAttribute(entity.colorId, typedValue, true);
            entity.v.setTextColor(ctx.getResources().getColor(typedValue.resourceId));
        }
        refreshStatusBar(ctx);
    }


    /**
     * 获取某个属性的TypedValue
     * @param ctx 上下文
     * @param attr  属性id
     * @return
     */
    public static TypedValue getAttrTypedValue(Activity ctx, int attr){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = ctx.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue;
    }


    /**
     * 刷新 StatusBar
     * @param ctx  上下文
     */
    private static void refreshStatusBar(Activity ctx) {
        if (Build.VERSION.SDK_INT >= 21) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = ctx.getTheme();
            theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
            ctx.getWindow().setStatusBarColor(ctx.getResources().getColor(typedValue.resourceId));
        }
    }
    /**
     * 展示切换动画
     */
    private static void showAnimation(Activity ctx) {
        final View decorView = ctx.getWindow().getDecorView();
        Bitmap cacheBitmap = getCacheBitmapFromView(decorView);
        if (decorView instanceof ViewGroup && cacheBitmap != null) {
            final View view = new View(ctx);
            view.setBackgroundDrawable(new BitmapDrawable(ctx.getResources(), cacheBitmap));

            ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            ((ViewGroup) decorView).addView(view, layoutParam);

            ValueAnimator objectAnimator = ValueAnimator.ofFloat(1f, 0f);//view, "alpha",
            objectAnimator.setDuration(500);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ViewGroup) decorView).removeView(view);

                }
            });
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (Float) animation.getAnimatedValue();
                    view.setAlpha(alpha);
                }
            });
            objectAnimator.start();
        }
    }
    /**
     * 获取一个 View 的缓存视图
     *
     * @param view
     * @return
     */
    private static Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    /**
     * 视图销毁时调用
     */
    public static void onDestory(){
        mBackGroundViews.clear();
        mTextColorViews.clear();
        mBackGroundDrawableViews.clear();
        mBackGroundViews = null;
        mTextColorViews = null;
        mBackGroundDrawableViews = null;
        mChangeModeController = null;
    }

    /**
     * 添加背景颜色属性
     * @param view
     * @param colorId
     * @return
     */
    public ChangeModeController addBackgroundColor(View view, int colorId) {
        mBackGroundViews.add(new AttrEntity(view,colorId));
        return this;
    }

    /**
     *添加背景图片属性
     * @param view
     * @param drawableId  属性id
     * @return
     */
    public ChangeModeController addBackgroundDrawable(View view, int drawableId) {
        mBackGroundDrawableViews.add(new AttrEntity(view,drawableId));
        return this;
    }

    /**
     * 添加字体颜色属性
     * @param view
     * @param colorId 属性id
     * @return
     */
    public ChangeModeController addTextColor(View view, int colorId) {
        mTextColorViews.add(new AttrEntity<TextView>((TextView) view,colorId));
        return this;
    }

    class AttrEntity<T>{
        T v;//控件
        int colorId;//属性id
        public AttrEntity(T v, int colorId) {
            this.v = v;
            this.colorId = colorId;
        }
    }
}
