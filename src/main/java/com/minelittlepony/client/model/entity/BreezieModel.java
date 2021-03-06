package com.minelittlepony.client.model.entity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.model.MsonPart;
import com.minelittlepony.mson.api.model.biped.MsonBiped;

import static com.minelittlepony.model.PonyModelConstants.PI;

public class BreezieModel<T extends LivingEntity> extends MsonBiped<T> implements MsonModel {

    private ModelPart neck;

    private ModelPart leftWing;
    private ModelPart rightWing;

    public BreezieModel() {
        textureHeight = 64;
    }

    @Override
    public void init(ModelContext context) {
        super.init(context);
        neck = context.findByName("neck");
        leftWing = context.findByName("left_wing");
        rightWing = context.findByName("right_wing");
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        helmet.visible = false;
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(neck, leftWing, rightWing));
    }

    @Override
    public void setAngles(T entity, float move, float swing, float ticks, float headYaw, float headPitch) {

        head.yaw = headYaw * 0.017453292F;
        head.pitch = headPitch * 0.017453292F;

        leftArm.pitch = MathHelper.cos(move * 0.6662F) * swing;
        leftArm.roll = 0;

        ((MsonPart)rightArm).rotate(swing * MathHelper.cos(move * 0.6662F + PI),        0, 0);
        ((MsonPart)leftLeg) .rotate(swing * MathHelper.cos(move * 0.6662F + PI) * 1.4F, 0, 0);
        ((MsonPart)rightLeg).rotate(swing * MathHelper.cos(move * 0.6662F)      * 1.4F, 0, 0);

        if (riding) {
            leftArm.pitch += -PI / 5;
            rightArm.pitch += -PI / 5;

            rotateLegRiding((MsonPart)leftLeg, -1);
            rotateLegRiding((MsonPart)rightLeg, 1);
        }

        rotateArm(leftArm, leftArmPose, 1);
        rotateArm(rightArm, rightArmPose, 1);

        if (handSwingProgress > 0) {
            swingArms(getPreferredArm(entity));
        }

        float rotX = MathHelper.sin(ticks * 0.067F) * 0.05F;
        float rotZ = MathHelper.cos(ticks * 0.09F) * 0.05F + 0.05F;

        leftArm.pitch -= rotX;
        leftArm.roll -= rotZ;

        rightArm.pitch += rotX;
        rightArm.roll += rotZ;

        rotX = MathHelper.sin(ticks * 0.3F) * 0.05F;
        rotZ = MathHelper.cos(ticks * 0.2F) * 0.05F + 0.05F;

        rotX -= 0.05F;

        leftWing.yaw = rotX * 10;
        leftWing.pitch = rotZ;
        rightWing.yaw = -rotX * 10;
        rightWing.pitch = rotZ;

        if (rightArmPose == ArmPose.BOW_AND_ARROW) {
            raiseArm(rightArm, leftArm, -1);
        } else if (leftArmPose == ArmPose.BOW_AND_ARROW) {
            raiseArm(leftArm, rightArm, 1);
        }
    }

    protected void rotateLegRiding(MsonPart leg, float factor) {
        leg.rotate(-1.4137167F, factor * PI / 10, factor * 0.07853982F);
    }

    protected void swingArms(Arm mainHand) {
        torso.yaw = MathHelper.sin(MathHelper.sqrt(handSwingProgress) * PI * 2) / 5;

        if (mainHand == Arm.LEFT) {
            torso.yaw *= -1;
        }

        float sin = MathHelper.sin(torso.yaw) * 5;
        float cos = MathHelper.cos(torso.yaw) * 5;

        leftArm.pitch += torso.yaw;
        leftArm.yaw += torso.yaw;
        leftArm.pivotX = cos;
        leftArm.pivotZ = -sin;

        rightArm.yaw += torso.yaw;
        rightArm.pivotX = -cos;
        rightArm.pivotZ = sin;

        float swingAmount = 1 - (float)Math.pow(1 - handSwingProgress, 4);

        float swingFactorX = MathHelper.sin(swingAmount * PI);
        float swingX = MathHelper.sin(handSwingProgress * PI) * (0.7F - head.pitch) * 0.75F;

        ModelPart mainArm = getArm(mainHand);
        mainArm.pitch -= swingFactorX * 1.2F + swingX;
        mainArm.yaw += torso.yaw * 2;
        mainArm.roll -= MathHelper.sin(handSwingProgress * PI) * 0.4F;
    }

    protected void rotateArm(ModelPart arm, ArmPose pose, float factor) {
        switch (pose) {
            case EMPTY:
                arm.yaw = 0;
                break;
            case ITEM:
                arm.pitch = arm.pitch / 2 - (PI / 10);
                arm.yaw = 0;
            case BLOCK:
                arm.pitch = arm.pitch / 2 - 0.9424779F;
                arm.yaw = factor * 0.5235988F;
                break;
            default:
        }
    }

    protected void raiseArm(ModelPart up, ModelPart down, float factor) {
        up.yaw = head.yaw + (factor / 10);
        up.pitch = head.pitch - (PI / 2);

        down.yaw = head.yaw - (factor / 2);
        down.pitch = head.pitch - (PI / 2);
    }
}
