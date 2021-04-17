package bkav.android.btalk.mutilsim;

import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.support.annotation.NonNull;

/**
 * Created by anhdt on 25/07/2017.
 */

class TriangleShape extends PathShape {
    private Path mTriangularPath;

    private TriangleShape(Path path, float stdWidth, float stdHeight) {
        super(path, stdWidth, stdHeight);
        mTriangularPath = path;
    }

    public static TriangleShape create(float width, float height, boolean isPointingUp) {
        Path triangularPath = new Path();
        if (isPointingUp) {
            triangularPath.moveTo(0, height);
            triangularPath.lineTo(width, height);
            triangularPath.lineTo(width / 2, 0);
            triangularPath.close();
        } else {
            triangularPath.moveTo(0, 0);
            triangularPath.lineTo(width / 2, height);
            triangularPath.lineTo(width, 0);
            triangularPath.close();
        }
        return new TriangleShape(triangularPath, width, height);
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        outline.setConvexPath(mTriangularPath);
    }
}
