package com.group_finity.mascot.config;

import com.group_finity.mascot.action.*;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.sound.SoundBuffer;
import com.group_finity.mascot.sound.SoundFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionBuilder implements IActionBuilder {

    private static final Logger log = Logger.getLogger(ActionBuilder.class.getName());

    private final String type;

    private final String name;

    private final String className;

    private final Integer voicePriority;

    private final SoundBuffer voice;

    private final Map<String, String> params = new LinkedHashMap<String, String>();

    private final List<AnimationBuilder> animationBuilders = new ArrayList<AnimationBuilder>();

    private final List<IActionBuilder> actionRefs = new ArrayList<IActionBuilder>();

    public ActionBuilder(final ImagePairLoader imagePairLoader, final SoundFactory soundFactory, final Configuration configuration, final Entry actionNode) throws IOException {
        this.name = actionNode.getAttribute("名前");
        this.type = actionNode.getAttribute("種類");
        this.className = actionNode.getAttribute("クラス");


        String voice = actionNode.getAttribute("voice");
        if (null != voice && voice.length() > 0) {
            this.voice = soundFactory.getSound(voice);
        } else {
            this.voice = null;
        }
        String priority = actionNode.getAttribute("priority");
        Integer pri = null;
        if (null != priority && priority.length() > 0) {
            try {
                pri = Integer.decode(priority);
            } catch (NumberFormatException ignored) {
            }
        }
        this.voicePriority = pri;


        log.log(Level.INFO, "動作読み込み開始({0})", this);

        this.getParams().putAll(actionNode.getAttributes());
        for (final Entry node : actionNode.selectChildren("アニメーション")) {
            this.getAnimationBuilders().add(new AnimationBuilder(imagePairLoader, soundFactory, node));
        }

        for (final Entry node : actionNode.getChildren()) {
            if (node.getName().equals("動作参照")) {
                this.getActionRefs().add(new ActionRef(imagePairLoader, soundFactory, configuration, node));
            } else if (node.getName().equals("動作")) {
                this.getActionRefs().add(new ActionBuilder(imagePairLoader, soundFactory, configuration, node));
            }
        }

        log.log(Level.INFO, "動作読み込み完了");
    }

    @Override
    public String toString() {
        return "動作(" + getName() + "," + getType() + "," + getClassName() + ")";
    }

    public Action buildAction(final Map<String, String> params) throws ActionInstantiationException {
        return buildAction(params, null, null);
    }

    @SuppressWarnings("unchecked")
    public Action buildAction(final Map<String, String> params, SoundBuffer voice, Integer voicePriority) throws ActionInstantiationException {

        try {
            // 変数マップを生成
            final VariableMap variables = createVariables(params);
            variables.put("voiceI", null == voice ? this.voice : voice);
            variables.put("voiceP", null == voicePriority ? this.voicePriority : voicePriority);

            // アニメーションを生成
            final List<Animation> animations = createAnimations();

            // 子アクションを生成
            final List<Action> actions = createActions();

            if (this.type.equals("組み込み")) {
                try {
                    final Class<? extends Action> cls = (Class<? extends Action>) Class.forName(this.getClassName());
                    try {

                        try {
                            return cls.getConstructor(List.class, VariableMap.class).newInstance(animations, variables);
                        } catch (final Exception e) {
                            // NOTE コンストラクタが無かったと思われるので次へ
                        }

                        return cls.getConstructor(VariableMap.class).newInstance(variables);
                    } catch (final Exception e) {
                        // NOTE コンストラクタが無かったと思われるので次へ
                    }

                    return cls.newInstance();
                } catch (final InstantiationException e) {
                    throw new ActionInstantiationException("動作クラスの初期化に失敗(" + this + ")", e);
                } catch (final IllegalAccessException e) {
                    throw new ActionInstantiationException("動作クラスにアクセスできません(" + this + ")", e);
                } catch (final ClassNotFoundException e) {
                    throw new ActionInstantiationException("動作クラスが見つかりません(" + this + ")", e);
                }

            } else if (this.type.equals("移動")) {
                return new Move(animations, variables);
            } else if (this.type.equals("静止")) {
                return new Stay(animations, variables);
            } else if (this.type.equals("固定")) {
                return new Animate(animations, variables);
            } else if (this.type.equals("複合")) {
                return new Sequence(variables, actions.toArray(new Action[actions.size()]));
            } else if (this.type.equals("選択")) {
                return new Select(variables, actions.toArray(new Action[actions.size()]));
            } else {
                throw new ActionInstantiationException("動作の種類が不明(" + this + ")");
            }

        } catch (final AnimationInstantiationException e) {
            throw new ActionInstantiationException("アニメーションの作成に失敗しました(" + this + ")", e);
        } catch (final VariableException e) {
            throw new ActionInstantiationException("パラメータの評価に失敗しました(" + this + ")", e);
        }
    }

    public void validate() throws ConfigurationException {

        for (final IActionBuilder ref : this.getActionRefs()) {
            ref.validate();
        }
    }

    private List<Action> createActions() throws ActionInstantiationException {
        final List<Action> actions = new ArrayList<Action>();
        for (final IActionBuilder ref : this.getActionRefs()) {
            actions.add(ref.buildAction(new HashMap<String, String>()));
        }
        return actions;
    }

    private List<Animation> createAnimations() throws AnimationInstantiationException {
        final List<Animation> animations = new ArrayList<Animation>();
        for (final AnimationBuilder animationFactory : this.getAnimationBuilders()) {
            animations.add(animationFactory.buildAnimation());
        }
        return animations;
    }

    private VariableMap createVariables(final Map<String, String> params) throws VariableException {
        final VariableMap variables = new VariableMap();
        for (final Map.Entry<String, String> param : this.getParams().entrySet()) {
            variables.put(param.getKey(), Variable.parse(param.getValue()));
        }
        for (final Map.Entry<String, String> param : params.entrySet()) {
            variables.put(param.getKey(), Variable.parse(param.getValue()));
        }
        return variables;
    }

    String getName() {
        return this.name;
    }

    private String getType() {
        return this.type;
    }

    private String getClassName() {
        return this.className;
    }

    private Map<String, String> getParams() {
        return this.params;
    }

    private List<AnimationBuilder> getAnimationBuilders() {
        return this.animationBuilders;
    }

    private List<IActionBuilder> getActionRefs() {
        return this.actionRefs;
    }


}
