<div dir="rtl">

# DECISIONS


> מלאו את הקובץ הזה. הוא חלק מההערכה — קצר וברור עדיף על ארוך.

## 1. החלטות ארכיטקטוניות
- אחת ההחלטות שלי בצד שרת היה לעשות הפרדה מוחלאת בין business logic לבין controller, עם טיפול רלוונטי לכל exception אפשרי.

## 2. הבאג ביתרת החופשה
- מה היה הבאג, איפה, ואיך תיקנתי:
הבאג שאחרי חישוב ימים מתוך הבקשה, הימים לא היו נבדקים בספירה כאשר עשו השוואה בין ימים משומשים ליתרה המותרת.
- הטסט שמוכיח את התיקון:
הוספתי טסט שמייצר עובד, מייצר לו בקשה מאושרת לימי חופש, ואז מנסה לייצר עוד בקשה שחורגת את היתרה של העובד.

## 3. אישור בקשה (approve) ו‑concurrency
- איך טיפלתי במצבים לא חוקיים (כבר אושר / לא קיים):
בדיקה שהאובייקט קיים, בדיקה שהוא במצב pending, ועדכון בDB
- מה לגבי אישור של שתי בקשות במקביל:
יצרתי SET שמכיל בקשות שבטיפול כדי לא לנסות להכניס בקשות נוספות, אך עדיין אפשר להכניס לרשימה כמה פעמים במקביל לכן הוספתי גם נעילה ברמת אובייקט כדי למנוע עבודה מקבילה על אותו אובייקט.

## 4. על מה ויתרתי בגלל הזמן
- ... ומה הייתי עושה עם עוד יום:
התעכבתי נורא בצד של השרת לכן רוב הזמן בבחינה הושקע בצד שרת, לא הספקתי להגיע לצד לקוח לכן נתתי יד חופשית לClaude לעבוד ולהשלים את המשימות, בנוסף לכך אין לי ידע בAngular לכן היה לי הכרחתי להיעזר בו בכדי להספיק כמה שיותר את המשימה למרות שהצד לקוח אינו מושלם.

## 5. שימוש ב‑AI
### איפה AI עזר (כולל prompts)
1. prompt:
The application is in angular, create a form that includes the next inputs:
startDate and endDate, input of type date, startDate can't be after endDate.
vacation type, input of type select (only one option can be selected), vacation types are: vacation, sick, unpaid.
add error with appropriate text for each input.

2. the page is stuck at loading and the leave requests aren't loading, the server is up and validated, data is returned, but in front it doesn't work

3. prompt: add input to choose employee from dropdown, retrieve the employees from [http://localhost:5080/api/employees]

4. prompt: fix the submit button, make sure that if the api returns an error, show the text.

### איפה דחיתי/תיקנתי הצעה של AI
- מה AI הציע, למה זה היה שגוי, ומה עשיתי במקום:

### אבטחה
- אם מצאתם בעיית אבטחה: מה מצאתם, איפה (קובץ + שורה), למה זו בעיה, ואיך תיקנתם:

## 6. הוראות הרצה
- (אם שיניתם משהו מהוראות ה‑README המקורי)

</div>
