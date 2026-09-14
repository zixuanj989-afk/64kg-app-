package com.zixuan.kg64;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

public class MainActivityV2 extends Activity {
    static final int INK=Color.rgb(23,25,22), CREAM=Color.rgb(245,243,234), LIME=Color.rgb(216,255,82), MUTED=Color.rgb(105,108,100);
    static final float TARGET=64f;
    static final int B=201,L=202,D=203,S=204;
    final LocalDate goal=LocalDate.of(2026,10,1);
    SharedPreferences p; LinearLayout root; TextView weight,gap,liquid,trend; EditText kcalInput; String movement="未记录";

    @Override public void onCreate(Bundle b){super.onCreate(b);p=getSharedPreferences("kg64_data",MODE_PRIVATE);getWindow().setStatusBarColor(CREAM);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);render();}

    void render(){
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true); sc.setBackgroundColor(CREAM);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(18),dp(18),dp(36)); sc.addView(root);
        TextView brand=t("64KG / DAILY",13,true,INK);brand.setLetterSpacing(.14f);root.addView(brand);root.addView(sp(8));root.addView(t("把今天做好，就在靠近。",25,true,INK));root.addView(sp(18));
        LinearLayout hero=row(); LinearLayout wb=stat("当前体重","--","kg"); weight=(TextView)((LinearLayout)wb.getChildAt(1)).getChildAt(0); hero.addView(wb,new LinearLayout.LayoutParams(0,dp(136),1)); hero.addView(stat("目标倒数",String.valueOf(Math.max(0,ChronoUnit.DAYS.between(LocalDate.now(),goal))),"天"),new LinearLayout.LayoutParams(0,dp(136),1));root.addView(card(hero,INK));root.addView(sp(14));
        LinearLayout mini=row(); gap=mini(mini,"距离 64KG","-- kg");mini(mini,"目标日期","10 / 01");liquid=mini(mini,"本周液断","剩 2 次");root.addView(card(mini,Color.WHITE));
        section("今日记录","数据保存在本机，再次打开仍会保留");
        LinearLayout wr=row();EditText wi=input("输入体重 kg",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);wr.addView(wi,new LinearLayout.LayoutParams(0,dp(52),1));Button rec=btn("记录",LIME,INK);rec.setOnClickListener(v->saveWeight(wi));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(92),dp(52));rp.setMargins(dp(10),0,0,0);wr.addView(rec,rp);root.addView(wr);root.addView(sp(10));
        kcalInput=input("今日已摄入 kcal",InputType.TYPE_CLASS_NUMBER);kcalInput.setText(p.getString("calories_"+LocalDate.now(),""));root.addView(kcalInput,new LinearLayout.LayoutParams(-1,dp(52)));root.addView(sp(10));root.addView(exercisePicker());root.addView(sp(10));Button sd=btn("保存今日完成情况",INK,Color.WHITE);sd.setOnClickListener(v->saveDay());root.addView(sd,new LinearLayout.LayoutParams(-1,dp(54)));
        section("体重趋势","最近记录");trend=t("",15,true,INK);trend.setLineSpacing(0,1.4f);root.addView(card(trend,Color.WHITE));
        section("液断日","每周最多 2 次");LinearLayout li=new LinearLayout(this);li.setOrientation(LinearLayout.VERTICAL);li.addView(t(isLiquid()?"今天是液断日":"今天正常饮食",19,true,INK));li.addView(sp(10));Button tg=btn(isLiquid()?"取消今天液断":"设为今天液断",LIME,INK);tg.setOnClickListener(v->toggleLiquid());li.addView(tg,new LinearLayout.LayoutParams(-1,dp(50)));li.addView(sp(8));Button rnd=btn("帮我随机安排本周液断",Color.WHITE,INK);rnd.setOnClickListener(v->randomLiquid());li.addView(rnd,new LinearLayout.LayoutParams(-1,dp(50)));root.addView(card(li,Color.WHITE));
        section("每日热量分配","点每一餐卡片即可上传并保存当天照片");root.addView(meal("breakfast",B,"早餐","300–350 kcal","鸡蛋 1–2 个 + 全麦面包 2 片 / 玉米 1 根 + 无糖豆浆"));root.addView(sp(8));root.addView(meal("lunch",L,"午餐","500–550 kcal","米饭 1 小碗 + 鸡胸 / 牛肉 / 鱼 120–150g + 两拳蔬菜"));root.addView(sp(8));root.addView(meal("dinner",D,"晚餐","400–450 kcal","杂粮饭半碗 + 虾 / 瘦肉 / 豆腐 120g + 两拳蔬菜"));root.addView(sp(8));root.addView(meal("snack",S,"加餐","100–150 kcal","苹果 / 橙子 1 个，或坚果 10g"));
        setContentView(sc);refresh();
    }

    void saveWeight(EditText e){String r=e.getText().toString().trim();if(r.isEmpty()){toast("先输入今天的体重");return;}float v;try{v=Float.parseFloat(r);}catch(Exception x){toast("体重格式不对");return;}if(v<35||v>200){toast("请确认体重数值");return;}String today=LocalDate.now().toString();List<String> keep=new ArrayList<>();for(String line:p.getString("history","").split(";"))if(!line.isEmpty()&&!line.startsWith(today+","))keep.add(line);keep.add(today+","+v);p.edit().putFloat("weight",v).putString("history",String.join(";",keep)).apply();e.setText("");refresh();toast("已记录 "+trim(v)+" kg");}

    void saveDay(){String raw=kcalInput.getText().toString().trim();int k=parseInt(raw);p.edit().putString("calories_"+LocalDate.now(),raw).putString("movement_"+LocalDate.now(),movement).apply();String m="今日记录已保存"+(k>1500?"\n摄入超过 1500 kcal。下一餐正常清淡即可。":"")+((movement.equals("未记录")||movement.equals("休息"))?"\n今天没有运动消耗记录。":"");if(k>1500||movement.equals("未记录")||movement.equals("休息"))new AlertDialog.Builder(this).setTitle("今日提醒").setMessage(m).setPositiveButton("知道了",null).show();else toast("今日记录已保存");}

    View exercisePicker(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.addView(t("今日运动",13,false,MUTED));box.addView(sp(7));LinearLayout cs=row();movement=p.getString("movement_"+LocalDate.now(),"未记录");for(String item:new String[]{"跑步","打球","休息"}){Button b=btn(item,item.equals(movement)?LIME:Color.WHITE,INK);b.setOnClickListener(v->{if(item.equals("跑步"))runDialog();else if(item.equals("打球"))ballDialog();else saveExercise("休息","休息 · 0 kcal",0);});LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(0,dp(46),1);q.setMargins(dp(3),0,dp(3),0);cs.addView(b,q);}box.addView(cs);box.addView(sp(8));String sum=p.getString("exercise_summary_"+LocalDate.now(),"");int burned=p.getInt("exercise_kcal_"+LocalDate.now(),0);if(sum.isEmpty())box.addView(t("点运动类型后输入运动量，我会自动估算消耗热量",12,false,MUTED));else{TextView s=t(sum,13,true,INK);s.setPadding(dp(12),dp(9),dp(12),dp(9));s.setBackground(round(Color.WHITE));box.addView(s);if(burned>0){TextView k=t("今日运动消耗约 "+burned+" kcal",12,true,INK);k.setPadding(0,dp(7),0,0);box.addView(k);}}return box;}

    void runDialog(){float w=p.getFloat("weight",0);if(w<=0){needWeight();return;}LinearLayout f=form();EditText mins=dinput("跑步时长（分钟）",InputType.TYPE_CLASS_NUMBER),dist=dinput("跑步距离（km，可选）",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);mins.setText(p.getString("run_minutes_"+LocalDate.now(),""));dist.setText(p.getString("run_distance_"+LocalDate.now(),""));f.addView(mins);f.addView(sp(8));f.addView(dist);f.addView(sp(8));f.addView(t("有距离时按体重×公里数估算；只填时长时按中等强度跑步估算。",11,false,MUTED));new AlertDialog.Builder(this).setTitle("记录跑步").setView(f).setPositiveButton("计算并保存",(d,x)->{int m=parseInt(mins.getText().toString());float km=parseFloat(dist.getText().toString());if(m<=0&&km<=0){toast("请至少输入时长或距离");return;}int k=km>0?Math.round(w*km):met(8.3f,w,m);String s="跑步"+(m>0?" · "+m+" 分钟":"")+(km>0?" · "+trim(km)+" km":"")+" · "+k+" kcal";p.edit().putString("run_minutes_"+LocalDate.now(),mins.getText().toString().trim()).putString("run_distance_"+LocalDate.now(),dist.getText().toString().trim()).apply();saveExercise("跑步",s,k);}).setNegativeButton("取消",null).show();}

    void ballDialog(){float w=p.getFloat("weight",0);if(w<=0){needWeight();return;}LinearLayout f=form();EditText mins=dinput("打球时长（分钟）",InputType.TYPE_CLASS_NUMBER);mins.setText(p.getString("ball_minutes_"+LocalDate.now(),""));f.addView(mins);f.addView(sp(10));f.addView(t("运动强度",12,true,INK));RadioGroup rg=new RadioGroup(this);rg.setOrientation(RadioGroup.HORIZONTAL);final float[] selected={p.getFloat("ball_met_"+LocalDate.now(),6f)};final String[] label={ballLabel(selected[0])};String[] labs={"轻松","中等","激烈"};float[] mets={4f,6f,8f};for(int i=0;i<3;i++){RadioButton r=new RadioButton(this);r.setText(labs[i]);r.setTextColor(INK);r.setTextSize(13);r.setChecked(Math.abs(selected[0]-mets[i])<.1f);final float mm=mets[i];final String ll=labs[i];r.setOnClickListener(v->{selected[0]=mm;label[0]=ll;});rg.addView(r,new RadioGroup.LayoutParams(0,-2,1));}f.addView(rg);f.addView(t("按球类运动强度估算，结果用于日常记录。",11,false,MUTED));new AlertDialog.Builder(this).setTitle("记录打球").setView(f).setPositiveButton("计算并保存",(d,x)->{int m=parseInt(mins.getText().toString());if(m<=0){toast("请输入打球时长");return;}int k=met(selected[0],w,m);String s="打球 · "+m+" 分钟 · "+label[0]+" · "+k+" kcal";p.edit().putString("ball_minutes_"+LocalDate.now(),mins.getText().toString().trim()).putFloat("ball_met_"+LocalDate.now(),selected[0]).apply();saveExercise("打球",s,k);}).setNegativeButton("取消",null).show();}

    void needWeight(){new AlertDialog.Builder(this).setTitle("先记录体重").setMessage("计算运动消耗需要当前体重，请先在上方记录今天的体重。").setPositiveButton("知道了",null).show();}
    void saveExercise(String type,String summary,int kcal){movement=type;p.edit().putString("movement_"+LocalDate.now(),type).putString("exercise_summary_"+LocalDate.now(),summary).putInt("exercise_kcal_"+LocalDate.now(),kcal).apply();render();}
    int met(float m,float w,int min){return Math.max(0,Math.round(m*3.5f*w/200f*min));}

    View meal(String key,int req,String title,String energy,String foods){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);LinearLayout h=row();h.addView(t(title,17,true,INK),new LinearLayout.LayoutParams(0,-2,1));TextView e=t(energy,13,true,INK);e.setPadding(dp(10),dp(5),dp(10),dp(5));e.setBackground(round(LIME));h.addView(e);box.addView(h);box.addView(sp(8));box.addView(t(foods,14,false,MUTED));box.addView(sp(9));String us=p.getString(photoKey(key),"");if(us.isEmpty()){TextView add=t("+ 点这里上传今天的"+title+"照片",12,true,INK);add.setGravity(Gravity.CENTER);add.setPadding(dp(10),dp(9),dp(10),dp(9));add.setBackground(round(CREAM));box.addView(add);}else{ImageView im=new ImageView(this);im.setScaleType(ImageView.ScaleType.CENTER_CROP);try{im.setImageURI(Uri.parse(us));}catch(Exception x){p.edit().remove(photoKey(key)).apply();}box.addView(im,new LinearLayout.LayoutParams(-1,dp(170)));box.addView(sp(7));TextView s=t("照片已保存 · 点击卡片可更换",11,false,MUTED);s.setGravity(Gravity.CENTER);box.addView(s);}View c=card(box,Color.WHITE);c.setOnClickListener(v->pickPhoto(req));return c;}
    void pickPhoto(int req){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,req);}
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null||data.getData()==null)return;String key=req==B?"breakfast":req==L?"lunch":req==D?"dinner":req==S?"snack":null;if(key==null)return;Uri u=data.getData();try{getContentResolver().takePersistableUriPermission(u,data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}p.edit().putString(photoKey(key),u.toString()).apply();toast("餐食照片已保存");render();}
    String photoKey(String k){return "meal_photo_"+LocalDate.now()+"_"+k;}

    void toggleLiquid(){String today=LocalDate.now().toString();List<String> ds=liquidDates();if(ds.contains(today))ds.remove(today);else if(weekLiquid(ds)>=2){toast("本周已经安排 2 次液断");return;}else ds.add(today);p.edit().putString("liquid_dates",String.join(",",ds)).apply();render();}
    void randomLiquid(){List<LocalDate> os=new ArrayList<>();LocalDate st=LocalDate.now().with(WeekFields.of(Locale.CHINA).dayOfWeek(),1);for(int i=0;i<7;i++){LocalDate d=st.plusDays(i);if(!d.isBefore(LocalDate.now()))os.add(d);}LocalDate pick=os.get(new Random().nextInt(os.size()));new AlertDialog.Builder(this).setTitle("本周随机液断日").setMessage("建议安排在 "+pick.getMonthValue()+" 月 "+pick.getDayOfMonth()+" 日。").setPositiveButton("设为液断日",(d,w)->{List<String> ds=liquidDates();if(weekLiquid(ds)<2&&!ds.contains(pick.toString())){ds.add(pick.toString());p.edit().putString("liquid_dates",String.join(",",ds)).apply();render();}}).setNegativeButton("取消",null).show();}
    List<String> liquidDates(){List<String> o=new ArrayList<>();for(String s:p.getString("liquid_dates","").split(","))if(!s.isEmpty())o.add(s);return o;}
    int weekLiquid(List<String> ds){int c=0;WeekFields wf=WeekFields.of(Locale.CHINA);LocalDate n=LocalDate.now();int wk=n.get(wf.weekOfWeekBasedYear()),yr=n.get(wf.weekBasedYear());for(String s:ds)try{LocalDate d=LocalDate.parse(s);if(d.get(wf.weekOfWeekBasedYear())==wk&&d.get(wf.weekBasedYear())==yr)c++;}catch(Exception ignored){}return c;}
    boolean isLiquid(){return liquidDates().contains(LocalDate.now().toString());}

    void refresh(){float w=p.getFloat("weight",0);weight.setText(w==0?"--":trim(w));gap.setText(w==0?"-- kg":trim(Math.max(0,w-TARGET))+" kg");liquid.setText("剩 "+Math.max(0,2-weekLiquid(liquidDates()))+" 次");String raw=p.getString("history","");if(raw.isEmpty())trend.setText("还没有体重记录");else{String[] ls=raw.split(";");StringBuilder b=new StringBuilder();for(int i=Math.max(0,ls.length-7);i<ls.length;i++){String[] q=ls[i].split(",");if(q.length==2)b.append(q[0].substring(5)).append("   ").append(q[1]).append(" kg\n");}trend.setText(b.toString().trim());}}

    LinearLayout form(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(22),dp(6),dp(22),dp(2));return f;}
    EditText dinput(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setInputType(type);e.setSingleLine();e.setTextSize(15);e.setPadding(dp(12),dp(8),dp(12),dp(8));e.setBackground(round(CREAM));return e;}
    int parseInt(String s){try{return Integer.parseInt(s.trim());}catch(Exception e){return 0;}}
    float parseFloat(String s){try{return Float.parseFloat(s.trim());}catch(Exception e){return 0f;}}
    String ballLabel(float m){return m<5?"轻松":m>=7?"激烈":"中等";}
    LinearLayout stat(String l,String v,String u){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setGravity(Gravity.CENTER);b.addView(t(l,12,false,Color.rgb(190,193,183)));LinearLayout ln=row();ln.setGravity(Gravity.CENTER);ln.addView(t(v,39,true,Color.WHITE));TextView unit=t(u,13,true,LIME);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(dp(6),dp(12),0,0);ln.addView(unit,p);b.addView(ln);return b;}
    TextView mini(LinearLayout parent,String l,String v){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setGravity(Gravity.CENTER);b.addView(t(l,11,false,MUTED));TextView x=t(v,16,true,INK);b.addView(x);parent.addView(b,new LinearLayout.LayoutParams(0,dp(78),1));return x;}
    void section(String a,String b){root.addView(sp(22));root.addView(t(a,21,true,INK));root.addView(sp(3));root.addView(t(b,13,false,MUTED));root.addView(sp(10));}
    LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    TextView t(String s,int z,boolean bold,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    EditText input(String h,int type){EditText e=new EditText(this);e.setHint(h);e.setTextSize(15);e.setInputType(type);e.setSingleLine();e.setPadding(dp(15),0,dp(15),0);e.setBackground(round(Color.WHITE));return e;}
    Button btn(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setTextColor(fg);b.setTextSize(13);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setGravity(Gravity.CENTER);b.setBackground(round(bg));return b;}
    View card(View v,int c){LinearLayout x=new LinearLayout(this);x.setPadding(dp(14),dp(12),dp(14),dp(12));x.setBackground(round(c));x.addView(v,new LinearLayout.LayoutParams(-1,-2));return x;}
    GradientDrawable round(int c){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(15));return g;}
    View sp(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return v;}
    int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
    String trim(float v){return v==(long)v?String.valueOf((long)v):String.format(Locale.CHINA,"%.1f",v);}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
