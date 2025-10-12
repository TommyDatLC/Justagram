package com.example.justagram.LoginAuth;

import android.content.res.Resources;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.view.animation.PathInterpolatorCompat;

public class IntroAnimator {


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
                logo.setScaleX(1.25f);
                logo.setScaleY(1.25f);

                // --- THAY ĐỔI 1: Lưu lại vị trí ban đầu của logo ---
                final float originalTranslationX = logo.getTranslationX();
                final float originalTranslationY = logo.getTranslationY();

                // --- Vị trí ban đầu cho 2 ảnh nền ---
                image1.setTranslationX(-500f);
                image1.setTranslationY(-500f);
                image2.setTranslationX(500f);
                image2.setTranslationY(-500f);

                // --- THAY ĐỔI 2: Căn giữa logo bằng TRANSLATION thay vì MARGIN ---
                // get the system info then get the screen info
                float screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                float screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

                // Lấy vị trí tuyệt đối (trên màn hình) của logo
                int[] logoLocation = new int[2];
                logo.getLocationOnScreen(logoLocation);
                float logoX = logoLocation[0];
                float logoY = logoLocation[1];

                // Tính toán khoảng cách cần di chuyển (translation)
                // để tâm của logo trùng với tâm của màn hình
               // float targetTranslationX = (screenWidth / 2f) - (logoX + logo.getWidth() / 2f);
                // Because of the pivot of image is on the top left change the pivot to the center by using (logoY + logoH / 2)
                float targetTranslationY = (screenHeight / 2f) - (logoY + logo.getHeight() / 2f);

                // Đặt vị trí translation mới cho logo (chưa animate)
                //logo.setTranslationX(targetTranslationX);
                logo.setTranslationY(targetTranslationY);


                // --- Vị trí cuối cùng của 2 ảnh nền ---
                float image1FinalX = screenWidth - image1.getWidth() / 2f;
                float image1FinalY = screenHeight - image1.getHeight() / 2f;
                float image2FinalX = -image2.getWidth() / 2f;
                float image2FinalY = screenHeight - image2.getHeight() / 2f;


                // == BƯỚC 2: ANIMATE 2 ẢNH NỀN VỚI VỊ TRÍ MỚI ==
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
                                                // --- THAY ĐỔI 3: Di chuyển logo về vị trí cũ bằng TRANSLATION ---
                                                logo.animate()
                                                        .translationX(originalTranslationX)
                                                        .translationY(originalTranslationY)
                                                        .setDuration(1000) // Thời gian quay về
                                                        .setInterpolator(PathInterpolatorCompat.create(0.7f, 0f, 0.3f, 1f))
                                                        .withEndAction(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                // == BƯỚC 5: HIỆN LAYOUT NỘI DUNG ==
                                                                contentLayout.animate()
                                                                        .alpha(1f)
                                                                        .setDuration(1000)
                                                                        .start();
                                                            }
                                                        }).start();
                                            }
                                        }).start();
                            }
                        }).start();
            }
        });
    }

}