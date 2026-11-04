package com.github.elenterius.spatialdb.geometry;

import com.github.elenterius.biomancy.util.serialization.NBTSerializer;
import com.github.elenterius.spatialdb.type.ShapeSerializers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Axis Aligned Ellipsoid made up of 8 segments (octant's)
 */
public class OctantEllipsoidShape implements Shape, HasRadius {
	private final Vec3 center;
	private final float radius;
	private final Vec3 origin;
	private final float aPos;
	private final float bPos;
	private final float cPos;
	private final float aNeg;
	private final float bNeg;
	private final float cNeg;

	public OctantEllipsoidShape(Vec3 pos, float aPos, float bPos, float cPos, float aNeg, float bNeg, float cNeg) {
		this.origin = pos;
		this.aPos = aPos;
		this.bPos = bPos;
		this.cPos = cPos;
		this.aNeg = aNeg;
		this.bNeg = bNeg;
		this.cNeg = cNeg;

		Vec3 min = new Vec3(origin.x - aNeg, origin.y - bNeg, origin.z - cNeg);
		Vec3 max = new Vec3(origin.x + aPos, origin.y + bPos, origin.z + cPos);
		center = min.lerp(max, 0.5d);

		float a = (aPos + aNeg) / 2f;
		float b = (bPos + bNeg) / 2f;
		float c = (cPos + cNeg) / 2f;
		radius = Math.max(Math.max(a, b), c);
	}

	public OctantEllipsoidShape(double x, double y, double z, float aPos, float bPos, float cPos, float aNeg, float bNeg, float cNeg) {
		this(new Vec3(x, y, z), aPos, bPos, cPos, aNeg, bNeg, cNeg);
	}

	@Override
	public boolean contains(double x, double y, double z) {
		double dx = origin.x - x;
		double dy = origin.y - y;
		double dz = origin.z - z;

		dx /= dx <= 0 ? aPos : aNeg;
		dy /= dy <= 0 ? bPos : bNeg;
		dz /= dz <= 0 ? cPos : cNeg;

		return dx * dx + dy * dy + dz * dz < 1; // x^2/a^2 + y^2/b^2 + z^2/c^2 = 1
	}

	@Override
	public boolean intersectsCuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		Vec3 closestPoint = GeometryUtil.closestPointOnAABB(minX, minY, minZ, maxX, maxY, maxZ, origin);
		return contains(closestPoint.x, closestPoint.y, closestPoint.z);
	}

	@Override
	public Vec3 center() {
		return center;
	}

	@Override
	public double distanceToSqr(double x, double y, double z) {
		return center.distanceToSqr(x, y, z);
	}

	@Override
	public AABB getAABB() {
		return new AABB(origin.x - aNeg, origin.y - bNeg, origin.z - cNeg, origin.x + aPos, origin.y + bPos, origin.z + cPos);
	}

	@Override
	public float getRadius() {
		return radius;
	}

	@Override
	public NBTSerializer<Shape> getNBTSerializer() {
		return ShapeSerializers.OCTANT_ELLIPSOID_SERIALIZER;
	}

	public record Serializer(String id) implements NBTSerializer<OctantEllipsoidShape> {

		@Override
		public CompoundTag write(OctantEllipsoidShape shape) {
			CompoundTag tag = new CompoundTag();

			tag.putDouble("X", shape.origin.x);
			tag.putDouble("Y", shape.origin.y);
			tag.putDouble("Z", shape.origin.z);

			tag.putFloat("A+", shape.aPos);
			tag.putFloat("B+", shape.bPos);
			tag.putFloat("C+", shape.cPos);
			tag.putFloat("A-", shape.aNeg);
			tag.putFloat("B-", shape.bNeg);
			tag.putFloat("C-", shape.cNeg);
			return tag;
		}

		@Override
		public OctantEllipsoidShape read(CompoundTag tag) {
			double x = tag.getDoubleOr("X", 0d);
			double y = tag.getDoubleOr("Y", 0d);
			double z = tag.getDoubleOr("Z", 0d);

			float aPos = tag.getFloatOr("A+", 0f);
			float bPos = tag.getFloatOr("B+", 0f);
			float cPos = tag.getFloatOr("C+", 0f);
			float aNeg = tag.getFloatOr("A-", 0f);
			float bNeg = tag.getFloatOr("B-", 0f);
			float cNeg = tag.getFloatOr("C-", 0f);

			return new OctantEllipsoidShape(x, y, z, aPos, bPos, cPos, aNeg, bNeg, cNeg);
		}
	}
}
