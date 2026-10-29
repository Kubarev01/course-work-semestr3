from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import math,shutil,json
P=Path(__file__).resolve().parents[1]; D=P/'docs/diagrams'
font='/System/Library/Fonts/Supplemental/Arial.ttf'; bold='/System/Library/Fonts/Supplemental/Arial Bold.ttf'
def diagram(name,size,boxes,lines,notes=[]):
 im=Image.new('RGB',size,'white'); g=ImageDraw.Draw(im)
 for pts,label,pos in lines:
  g.line(pts,fill='#374151',width=3)
  a,b=pts[-2:];angle=math.atan2(b[1]-a[1],b[0]-a[0]);q=16
  g.polygon([b,(b[0]-q*math.cos(angle-.45),b[1]-q*math.sin(angle-.45)),(b[0]-q*math.cos(angle+.45),b[1]-q*math.sin(angle+.45))],fill='#374151')
  if label:g.text(pos,label,font=ImageFont.truetype(font,27),fill='black')
 for x,y,w,h,title,rows in boxes:
  g.rectangle((x,y,x+w,y+h),fill='white',outline='#111827',width=3);g.rectangle((x,y,x+w,y+57),fill='#eef2f6',outline='#111827',width=3)
  g.text((x+18,y+13),title,font=ImageFont.truetype(bold,28),fill='black')
  for i,t in enumerate(rows):g.text((x+18,y+73+i*40),t,font=ImageFont.truetype(font,26),fill='black')
 for t,xy in notes:g.text(xy,t,font=ImageFont.truetype(font,26),fill='black')
 im.save(D/(name+'.png'))
diagram('database',(1800,1150),[
 (40,60,480,300,'USERS',['PK id BIGINT','username VARCHAR(80) UNIQUE','display_name VARCHAR(80)','password VARCHAR(60)']),
 (40,680,480,220,'USER_ROLES',['FK user_id → USERS.id','role ENUM','UNIQUE (user_id, role)']),
 (650,60,500,380,'RELEASES',['PK id BIGINT; version BIGINT','name VARCHAR(100)','description VARCHAR(1500)','target_date DATE; status ENUM','FK owner_id → USERS.id']),
 (1230,550,530,460,'TASKS',['PK id BIGINT; version BIGINT','title VARCHAR(120)','description VARCHAR(2000)','days DECIMAL(7,2)','daily_rate DECIMAL(12,2)','due_date DATE; status; priority','FK release_id → RELEASES.id','FK owner_id → USERS.id']),
 (40,990,1000,135,'USER_ACTIONS',['id; date_actions; actor; action; description'])],
 [([(280,360),(280,680)],'1 → *',(290,500)), ([(520,210),(650,210)],'1 → *',(535,160)), ([(1150,250),(1490,250),(1490,550)],'1 → *',(1505,370)), ([(520,310),(590,310),(590,790),(1230,790)],'1 → *',(740,748))],
 [('actor — текстовый логин для истории',(1080,1055))])
diagram('classes',(1800,1230),[
 (40,80,490,300,'AppUser',['- id: Long','- username, password: String','- roles: Set<Role>','+ isAdmin(): boolean','+ canWrite(): boolean']),
 (650,80,490,300,'Release',['- id, version: Long','- owner: AppUser','- targetDate: LocalDate','- status: ReleaseStatus']),
 (1240,490,520,400,'Task',['- id, version: Long','- owner: AppUser','- release: Release','- days, dailyRate: BigDecimal','- status: TaskStatus','+ getCost(): BigDecimal','+ isOverdue(): boolean']),
 (40,580,490,260,'UserAction',['- id: Long','- dateActions: Instant','- actor, action: String','- description: String']),
 (650,940,1110,210,'CalculationService',['+ calculate(tasks, people, reserve, start): Estimate','Estimate: days, remainingDays, baseCost, budget,','progress, finish'])],
 [([(530,210),(650,210)],'1     *',(555,165)), ([(1140,210),(1500,210),(1500,490)],'1 → *',(1520,340)), ([(530,330),(590,330),(590,680),(1240,680)],'1 → *',(810,635)), ([(1500,940),(1500,890)],'использует',(1580,900))],
 [('Role, TaskStatus, Priority и ReleaseStatus — перечисления',(50,35)),('Показаны основные классы предметной области и расчёта',(50,1180))])
diagram('mvc',(1800,980),[
 (40,60,370,170,'Браузер',['HTML формы','HTTP GET / POST']),
 (520,60,540,170,'SecurityFilterChain',['Сеанс, вход, CSRF','Правила доступа по URL']),
 (1180,60,560,170,'DispatcherServlet',['Маршрутизация запроса','Выбор контроллера']),
 (1180,370,560,200,'Controller',['DTO + @Valid','Model / redirect']),
 (520,370,540,200,'Service',['Владелец, роли, транзакции','Бизнес правила и расчёты']),
 (40,370,370,200,'Repository',['Spring Data JPA','Hibernate → H2']),
 (520,740,540,170,'Thymeleaf View',['Сборка HTML из Model','Экранирование значений'])],
 [([(410,140),(520,140)],'',(0,0)), ([(1060,140),(1180,140)],'',(0,0)), ([(1450,230),(1450,370)],'',(0,0)), ([(1180,465),(1060,465)],'',(0,0)), ([(520,465),(410,465)],'',(0,0)), ([(1450,570),(1450,820),(1060,820)],'Model',(1220,770)), ([(520,820),(20,820),(20,260),(80,260),(80,230)],'HTML',(270,765))])
