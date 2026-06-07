package slimeknights.mantle.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.Collection;
import java.util.Locale;

/**
 * Command to set a player's hunger to another value, as all creative mode
 * methods of changing hunger are slow.
 */
public class HungerCommand {
  // Reflection fields for accessing private FoodData fields in 1.21.1
  private static final java.lang.reflect.Field FOOD_LEVEL_FIELD;
  private static final java.lang.reflect.Field SATURATION_LEVEL_FIELD;

  static {
    try {
      FOOD_LEVEL_FIELD = FoodData.class.getDeclaredField("foodLevel");
      FOOD_LEVEL_FIELD.setAccessible(true);
      SATURATION_LEVEL_FIELD = FoodData.class.getDeclaredField("saturationLevel");
      SATURATION_LEVEL_FIELD.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Failed to reflect FoodData fields", e);
    }
  }

  private static int getFoodLevel(FoodData food) {
    try {
      return FOOD_LEVEL_FIELD.getInt(food);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to get foodLevel", e);
    }
  }

  private static void setFoodLevel(FoodData food, int value) {
    try {
      FOOD_LEVEL_FIELD.setInt(food, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to set foodLevel", e);
    }
  }

  private static float getSaturationLevel(FoodData food) {
    try {
      return SATURATION_LEVEL_FIELD.getFloat(food);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to get saturationLevel", e);
    }
  }

  private static void setSaturationLevel(FoodData food, float value) {
    try {
      SATURATION_LEVEL_FIELD.setFloat(food, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to set saturationLevel", e);
    }
  }

  /**
   * Registers this sub command with the root command
   * 
   * @param subCommand Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand = subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS));
    for (Operation operation : Operation.values()) {
      operation.register(subCommand);
    }
  }

  /** List of each operation to run */
  @RequiredArgsConstructor
  private enum Operation {
    SET(20) {
      @Override
      public void apply(FoodData food, int hunger, float saturation) {
        setFoodLevel(food, hunger);
        setSaturationLevel(food, Math.min(hunger, saturation));
      }
    },
    ADD(1) {
      @Override
      public void apply(FoodData food, int hunger, float saturation) {
        food.eat(hunger, saturation);
      }
    },
    SUBTRACT(0) {
      @Override
      public void apply(FoodData food, int hunger, float saturation) {
        int newFoodLevel = Math.max(0, getFoodLevel(food) - hunger);
        setFoodLevel(food, newFoodLevel);
        setSaturationLevel(food, Mth.clamp(getSaturationLevel(food) - hunger * saturation * 2, 0, newFoodLevel));
      }
    };

    private final String name = this.name().toLowerCase(Locale.ROOT);
    /** Saturation used if its unset */
    private final float defaultSaturation;

    /** Registers this argument with the builder */
    public void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
      subCommand.then(Commands.literal(name)
          .then(Commands.argument("targets", EntityArgument.players())
              .then(Commands.argument("hunger", IntegerArgumentType.integer(0, 20))
                  .executes(context -> run(context, defaultSaturation))
                  .then(Commands.argument("saturation", FloatArgumentType.floatArg(0))
                      .executes(context -> run(context, FloatArgumentType.getFloat(context, "saturation")))))));
    }

    /** Applies the arguments to the food stats */
    public abstract void apply(FoodData food, int hunger, float saturation);

    /** Applies this to the given context */
    public int run(CommandContext<CommandSourceStack> context, float saturation) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
      int hunger = IntegerArgumentType.getInteger(context, "hunger");

      // apply to each player
      for (Player player : players) {
        apply(player.getFoodData(), hunger, saturation);
      }

      // log success
      if (players.size() == 1) {
        Player player = players.iterator().next();
        context.getSource().sendSuccess(
            () -> Component.translatable("command.mantle.hunger." + name + ".single", player.getName(), hunger), true);
      } else {
        context.getSource().sendSuccess(
            () -> Component.translatable("command.mantle.hunger." + name + ".multiple", players.size(), hunger), true);
      }
      return players.size();
    }
  }
}
