package net.malisis.doors.renderer.block;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.Door;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class MixedBlockRenderer extends BaseRenderer
{
	public static int renderId;
	public Door block;
	private static int currentPass;

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		if(!item.hasTagCompound())
			return;
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

		prepare(TYPE_ITEM_INVENTORY);

		Block b1 = Block.getBlockById(item.getTagCompound().getInteger("block1"));
		Block b2 = Block.getBlockById(item.getTagCompound().getInteger("block2"));

		int metadata1 = item.getTagCompound().getInteger("metadata1");
		int metadata2 = item.getTagCompound().getInteger("metadata2");

		set(b1, metadata1);
		drawShape(ShapePreset.Cube());
		set(b2, metadata2);
		drawSecondPass(ForgeDirection.WEST);

		clean();

		GL11.glPopMatrix();

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		set(world, block, x, y, z, metadata);

		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		if (te == null)
			return false;


		
		prepare(TYPE_WORLD);
		
		if(renderer.hasOverrideBlockTexture())
		{
			RenderParameters rp = RenderParameters.setDefault();
			rp.icon = renderer.overrideBlockTexture;
			drawShape(ShapePreset.Cube(), rp);
		}
		else if (currentPass == 0 && te.block1 != null)
		{
			set(te.block1, te.metadata1);
			drawShape(ShapePreset.Cube());
		}
		else if (te.block2 != null)
		{
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			set(te.block2, te.metadata2);
			drawSecondPass(ForgeDirection.getOrientation(metadata));
		}
		clean();

		return true;
	}

	private void drawSecondPass(ForgeDirection dir)
	{
		RenderParameters rp = RenderParameters.setDefault();
		rp.usePerVertexAlpha = true;

		Shape cube = ShapePreset.Cube();
		for (Face f : cube.getFaces())
		{
			for (Vertex v : f.getVertexes())
			{
				if (v.name().toLowerCase().contains(dir.name().toLowerCase()))
					v.setAlpha(0);
			}
		}
		drawShape(cube, rp);
	}

	public static void setRenderPass(int pass)
	{
		currentPass = pass;
	}

}