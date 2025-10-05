package com.example.justagram;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class IntroAnimator {

    // Interface để xử lý callback khi animation kết thúc
    interface AnimationEndListener {
        void onAnimationEnd();
    }

    public static void start(
            final ImageView logo,
            final ImageView image1,
            final ImageView image2,
            final LinearLayout contentLayout
    ) {
        // Sử dụng post để đảm bảo tất cả các view đã được đo lường kích thước
        logo.post(new Runnable() {
            @Override
            public void run() {
                // == BƯỚC 0: THIẾT LẬP TRẠNG THÁI BAN ĐẦU ==
                logo.setAlpha(0f);
                contentLayout.setAlpha(0f);
                logo.setScaleX(2);
                logo.setScaleY(2);

                // --- THAY ĐỔI 1: Cập nhật vị trí ban đầu cho 2 ảnh nền ---
                image1.setTranslationX(-500f);
                image1.setTranslationY(-500f); // Sửa từ 500f -> -500f
                image2.setTranslationX(500f);  // Sửa từ -500f -> 500f
                image2.setTranslationY(-500f);

                // == BƯỚC 1: ĐƯA LOGO VÀO GIỮA MÀN HÌNH ==
                centerViewOnScreen(logo);

                // --- THAY ĐỔI 2: Tính toán vị trí cuối cùng dựa trên kích thước màn hình ---
                float screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                float screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

                float image1FinalX = screenWidth - 750 ;
                float image1FinalY = screenHeight - 750;
                float image2FinalX = -750 ;
                float image2FinalY = screenHeight - 750;


                // == BƯỚC 2: ANIMATE 2 ẢNH NỀN VỚI VỊ TRÍ MỚI ==
                // --- THAY ĐỔI 3: Cập nhật animation ---
                image1.animate()
                        .translationX(image1FinalX)
                        .translationY(image1FinalY)
                        .setDuration(2000)
                        .start();

                image2.animate()
                        .translationX(image2FinalX)
                        .translationY(image2FinalY)
                        .setDuration(2000)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                // == BƯỚC 3: HIỆN VÀ THU NHỎ LOGO ==
                                logo.animate()
                                        .alpha(1f)
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(1000)
                                        .setInterpolator(new DecelerateInterpolator(2f))
                                        .withEndAction(new Runnable() {

                                            @Override
                                            public void run() {

                                                // == BƯỚC 4: DI CHUYỂN LOGO VỀ VỊ TRÍ CŨ ==
                                                animateMargins(logo, 0, 0, new AnimationEndListener() {
                                                    @Override
                                                    public void onAnimationEnd() {
                                                        // == BƯỚC 5: HIỆN LAYOUT NỘI DUNG ==
                                                        contentLayout.animate()
                                                                .alpha(1f)
                                                                .setDuration(1000)
                                                                .start();
                                                    }
                                                });
                                            }
                                        }).start();
                            }
                        }).start();
            }
        });
    }

    /**
     * Hàm này tính toán và đặt margin để một view bất kỳ nằm chính giữa màn hình.
     */
    private static void centerViewOnScreen(View view) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        View parent = (View) view.getParent();
        int[] parentLocation = new int[2];
        parent.getLocationOnScreen(parentLocation);
        int parentLeft = parentLocation[0];
        int parentTop = parentLocation[1];

        int marginLeft = (screenWidth / 2) - (viewWidth / 2) - parentLeft;
        int marginTop = (screenHeight / 2) - (viewHeight / 2) - parentTop;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(marginLeft, marginTop, params.rightMargin, params.bottomMargin);
        view.setLayoutParams(params);
    }

    /**
     * Hàm này tạo animation cho thuộc tính margin.
     */
    private static void animateMargins(final View view, int toLeft, int toTop, final AnimationEndListener listener) {
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int startLeft = params.leftMargin;
        int startTop = params.topMargin;

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setInterpolator(new PathInterpolator(0.7f, 0f, 0.3f, 1)); //https://cubic-bezier.com/#0,.73,.22,.94
        animator.setDuration(1000);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = (float) valueAnimator.getAnimatedValue();
                int currentLeft = (int) (startLeft * fraction + toLeft * (1 - fraction));
                int currentTop = (int) (startTop * fraction + toTop * (1 - fraction));
                params.setMargins(currentLeft, currentTop, params.rightMargin, params.bottomMargin);
                view.requestLayout();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animator.start();
    }
}