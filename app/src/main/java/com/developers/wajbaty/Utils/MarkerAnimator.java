package com.developers.wajbaty.Utils;

import com.google.android.gms.maps.model.Marker;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;


public class MarkerAnimator {

//        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
//        public static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
//            final LatLng startPosition = marker.getPosition();
//            final Handler handler = new Handler();
//            final long start = SystemClock.uptimeMillis();
//            final Interpolator interpolator = new AccelerateDecelerateInterpolator();
//            final float durationInMs = 3000;
//
//            handler.post(new Runnable() {
//                long elapsed;
//                float t;
//                float v;
//
//                @Override
//                public void run() {
//                    // Calculate progress using interpolator
//                    elapsed = SystemClock.uptimeMillis() - start;
//                    t = elapsed / durationInMs;
//                    v = interpolator.getInterpolation(t);
//
//                    marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));
//
//                    // Repeat till progress is complete.
//                    if (t < 1) {
//                        // Post again 16ms later.
//                        handler.postDelayed(this, 16);
//                    }
//                }
//            });
//        }

//        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//        public  static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
//            final LatLng startPosition = marker.getPosition();
//
//            ValueAnimator valueAnimator = new ValueAnimator();
//            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float v = animation.getAnimatedFraction();
//                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
//                    marker.setPosition(newPosition);
//                }
//            });
//            valueAnimator.setFloatValues(0, 1); // Ignored.
//            valueAnimator.setDuration(3000);
//            valueAnimator.start();
//        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public static void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
            TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
                @Override
                public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                    return latLngInterpolator.interpolate(fraction, startValue, endValue);
                }
            };
            Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
            ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
            animator.setDuration(3000);
            animator.start();
        }

    public interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class Linear implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lng = (b.longitude - a.longitude) * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;

                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }

        class Spherical implements LatLngInterpolator {

            /* From github.com/googlemaps/android-maps-utils */
            @Override
            public LatLng interpolate(float fraction, LatLng from, LatLng to) {
                // http://en.wikipedia.org/wiki/Slerp
                double fromLat = toRadians(from.latitude);
                double fromLng = toRadians(from.longitude);
                double toLat = toRadians(to.latitude);
                double toLng = toRadians(to.longitude);
                double cosFromLat = cos(fromLat);
                double cosToLat = cos(toLat);

                // Computes Spherical interpolation coefficients.
                double angle = computeAngleBetween(fromLat, fromLng, toLat, toLng);
                double sinAngle = sin(angle);
                if (sinAngle < 1E-6) {
                    return from;
                }
                double a = sin((1 - fraction) * angle) / sinAngle;
                double b = sin(fraction * angle) / sinAngle;

                // Converts from polar to vector and interpolate.
                double x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng);
                double y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng);
                double z = a * sin(fromLat) + b * sin(toLat);

                // Converts interpolated vector back to polar.
                double lat = atan2(z, sqrt(x * x + y * y));
                double lng = atan2(y, x);
                return new LatLng(toDegrees(lat), toDegrees(lng));
            }

            private double computeAngleBetween(double fromLat, double fromLng, double toLat, double toLng) {
                // Haversine's formula
                double dLat = fromLat - toLat;
                double dLng = fromLng - toLng;
                return 2 * asin(sqrt(pow(sin(dLat / 2), 2) +
                        cos(fromLat) * cos(toLat) * pow(sin(dLng / 2), 2)));
            }
        }
    }

}
