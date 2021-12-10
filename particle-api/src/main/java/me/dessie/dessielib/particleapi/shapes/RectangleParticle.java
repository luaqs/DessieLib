package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.transform.TransformType;
import me.dessie.dessielib.particleapi.transform.transformations.ParticleRotate;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;

public class RectangleParticle extends Shape {
    public RectangleParticle(ParticleData data, int points, double width, double height, Orientation... orientations) {
        super(data, points, ((location, step) -> {

            //Make sure all particles are the same distance apart by using width/height proportions.
            double widthProportion = (width + height) / width;
            double heightProportion = (width + height) / height;

            //Find how many particles should be draw on the width and height lines.
            int pointsPerWidth = (int) (points / widthProportion) / 2;
            int pointsPerHeight = (int) (points / heightProportion) / 2;

            //Calculate the current step of each.
            double widthStep = width / pointsPerWidth * (pointsPerWidth - (step % pointsPerWidth));
            double heightStep = height / pointsPerHeight * (pointsPerHeight - (step % pointsPerHeight));

            //Draw them depending on which line of the Rectangle we're drawing.
            if(step < pointsPerWidth) {
                return new Point3D(location.getX() + widthStep, location.getY(), location.getZ());
            } else if(step < pointsPerWidth + pointsPerHeight) {
                return new Point3D(location.getX() + width, location.getY() + heightStep, location.getZ());
            } else if(step < pointsPerHeight + pointsPerWidth * 2) {
                return new Point3D(location.getX() + width - widthStep, location.getY() + height, location.getZ());
            } else {
                return new Point3D(location.getX(), location.getY() + height - heightStep, location.getZ());
            }
        }));

        //Apply the Orientations, since the Rectangle is always drawn in a static Orientation.
        //These can be used to make the Rectangle flat, or rotated.
        for(Orientation orientation : orientations) {
            this.addTransform(new ParticleRotate(TransformType.STATIC, 1, ((location, step) -> {
                switch(orientation.getAxis()) {
                    case X -> {
                        return new Point3D(orientation.getRotation(), 0,0);
                    }
                    case Y -> {
                        return new Point3D(0, orientation.getRotation(), 0);
                    }
                    case Z -> {
                        return new Point3D(0, 0, orientation.getRotation());
                    }
                }

                return new Point3D(0, 0, 0);
            })));
        }
    }

    public enum Axis {
        X, Y, Z;
    }

    public static class Orientation {
        private final Axis axis;
        private final double rotation;
        public Orientation(Axis axis, double rotation) {
            this.axis = axis;
            this.rotation = rotation;
        }

        public Axis getAxis() {
            return axis;
        }

        public double getRotation() {
            return rotation;
        }
    }
}
