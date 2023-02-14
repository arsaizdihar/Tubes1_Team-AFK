package Processors;

import Enums.ObjectTypes;
import Enums.PlayerActions;
import Models.ActionWeight;
import Models.GameObject;
import Models.GameState;
import Models.Position;
import Services.MathService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FoodProcessor extends Processor {
    final static double VALUE = 3.0;


    public FoodProcessor(GameObject bot, GameState gameState) {
        super(bot, gameState);
    }

    @Override
    public void process() {
        var botPos = bot.getProjectedPosition(1);
        var obstacle = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD).collect(Collectors.toList());
        var filtered = gameState.getGameObjects()
                .stream()
                .filter(item -> filterFood(item, obstacle, botPos))
                .collect(Collectors.toList());
        var _nearest = filtered.stream()
                .min(Comparator.comparing(item -> MathService.getDistanceBetween(botPos, item.getPosition()) - bot.getSize() - item.getSize()));
        if (_nearest.isEmpty()) {
            return;
        }
        var nearest = _nearest.get();

        var array = new ArrayList<ActionWeight>(1);
        this.data.put(PlayerActions.Forward, array);
        var heading = MathService.getHeadingBetween(botPos, nearest.getPosition());

        if (MathService.getDegreeDifference(heading, bot.currentHeading) > 150) {
            heading = bot.currentHeading;
        }
        // TODO: cek di currentHeading ada food atau ngga, kasih treshold misal 30 derajat
        var actionWeight = new ActionWeight(heading, VALUE);
        array.add(actionWeight);
    }


    boolean filterFood(GameObject item, List<GameObject> obstacles, Position botPos) {
        if (item.getGameObjectType() != ObjectTypes.FOOD && item.getGameObjectType() != ObjectTypes.SUPER_FOOD)
            return false;
        var worldCenterDis = MathService.getDistanceBetween(item.getPosition(), gameState.world.centerPoint);
        var curWorldCenterDis = MathService.getDistanceBetween(botPos, gameState.world.centerPoint);
        // Jangan mengambil food yang di luar map
        if (worldCenterDis + bot.getSize() >= gameState.world.radius ||
                (gameState.world.currentTick <= 100 && worldCenterDis <= Math.min(gameState.world.radius / 2.5, curWorldCenterDis))) {
            return false;
        }
        for (var obs : obstacles) {
            if (MathService.isCollide(item, obs)) return false;
        }
        return true;
    }

}
