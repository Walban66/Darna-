package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection

object Localization {
    var isArabic by mutableStateOf(true) // Arabic is default

    val layoutDirection: LayoutDirection
        get() = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    fun get(key: String): String {
        return if (isArabic) {
            arabicMap[key] ?: englishMap[key] ?: key
        } else {
            englishMap[key] ?: arabicMap[key] ?: key
        }
    }

    private val arabicMap = mapOf(
        "app_name" to "دارنا",
        "app_slogan" to "المنصة الذكية لإدارة العائلة والمنزل",
        
        // Splash & Onboarding
        "get_started" to "ابدأ الآن",
        "skip" to "تخطي",
        "next" to "التالي",
        "onboarding_1_title" to "قائمة المشتريات المشتركة",
        "onboarding_1_desc" to "شارك عائلتك في تدوين المشتريات وتتبع ما تم شراؤه لحظة بلحظة.",
        "onboarding_2_title" to "جرد ومخزون المنزل",
        "onboarding_2_desc" to "راقب مخزون مطبخك مع تتبع تواريخ الصلاحية وحالة توفر السلع.",
        "onboarding_3_title" to "تخطيط الوجبات بالذكاء الاصطناعي",
        "onboarding_3_desc" to "احصل على اقتراحات وجبات ذكية بناءً على المكونات المتوفرة لديك.",
        
        // Auth Flow
        "login_title" to "تسجيل الدخول",
        "register_title" to "إنشاء حساب جديد",
        "forgot_password" to "نسيت كلمة المرور؟",
        "email_label" to "البريد الإلكتروني",
        "password_label" to "كلمة المرور",
        "phone_label" to "رقم الهاتف",
        "name_label" to "الاسم الكامل",
        "login_button" to "دخول",
        "register_button" to "إنشاء حساب",
        "google_login" to "تسجيل بواسطة Google",
        "phone_login" to "تسجيل بواسطة الهاتف",
        "or_divider" to "أو من خلال",
        "no_account" to "ليس لديك حساب؟ سجل الآن",
        "has_account" to "لديك حساب بالفعل؟ سجل الدخول",
        "reset_password_btn" to "إرسال رابط إعادة التعيين",
        "reset_sent_msg" to "تم إرسال تعليمات إعادة تعيين كلمة المرور إلى بريدك الإلكتروني.",
        "invalid_credentials" to "خطأ في بيانات الدخول، يرجى المحاولة مرة أخرى.",

        // Family Setup
        "family_setup_title" to "إعداد العائلة",
        "family_create_title" to "إنشاء عائلة جديدة",
        "family_join_title" to "انضمام إلى عائلة قائمة",
        "family_name_label" to "اسم العائلة (مثال: عائلة أحمد)",
        "invite_code_label" to "رمز الدعوة للعائلة",
        "create_family_btn" to "إنشاء عائلة",
        "join_family_btn" to "انضمام للعائلة",
        "enter_invite_code" to "أدخل رمز الدعوة المكون من 6 رموز",
        "invite_code_helper" to "شارك هذا الرمز مع أفراد عائلتك للانضمام إليك.",
        "invalid_invite" to "رمز الدعوة غير صالح، يرجى التحقق وإعادة المحاولة.",

        // Dashboard Screen
        "dashboard_tab" to "الرئيسية",
        "welcome" to "مرحباً بك،",
        "role" to "الدور",
        "admin" to "مدير العائلة",
        "member" to "أحد الـمـنـتـسـبـيـن",
        "family_summary" to "ملخص عائلة",
        "shopping_summary" to "مشتريات معلقة",
        "inventory" to "سلع المخزون",
        "expiring_soon" to "توشك على الانتهاء",
        "low_stock" to "قليلة المخزون",
        "upcoming_tasks" to "المهام القادمة",
        "suggested_meals" to "وجبة مقترحة اليوم",
        "recent_activities" to "آخر نشاط للعائلة",
        "monthly_expenses" to "مصروفات الشهر الحالي",
        "quick_access" to "الوصول السريع",

        // Shopping List Screen
        "shopping_tab" to "المشتريات",
        "add_item" to "إضافة مادة",
        "category" to "الفئة",
        "quantity" to "الكمية",
        "unit" to "الوحدة",
        "purchased" to "تم شراؤه",
        "pending" to "قيد الانتظار",
        "added_by" to "أضيف بواسطة",
        "item_name_label" to "اسم المنتج",
        "search_shopping" to "ابحث عن منتج...",
        "by_category" to "حسب الفئة",

        // Inventory Screen
        "inventory_tab" to "المخزون",
        "expiry_date" to "تاريخ انتهاء الصلاحية",
        "add_product" to "إضافة سلعة",
        "scan_barcode" to "مسح باركود",
        "scan_success" to "تم التعرف على الباركود بنجاح",
        "product_details" to "تفاصيل المنتج",
        "threshold" to "الحد الأدنى للتنبيه",
        "expired" to "منتهي الصلاحية",
        "unlimited" to "غير محدد",

        // Meal Screen
        "meals_tab" to "الوجبات",
        "meal_planner" to "مخطط الوجبات",
        "ai_suggestions" to "اقتراحات وجبات بالذكاء الاصطناعي",
        "breakfast" to "الفطور",
        "lunch" to "الغداء",
        "dinner" to "العشاء",
        "add_meal" to "إضافة وجبة",
        "recipe_details" to "تفاصيل الوصفة",
        "ingredients" to "المكونات والمقادير",
        "instructions" to "طريقة التحضير",
        "generate_ai_meals" to "توليد وجبات ذكية باستخدام الذكاء الاصطناعي",
        "generating_ai" to "جاري تحليل المخزون وتوليد الوجبات...",
        "ai_results_desc" to "وصفات لذيذة تم توليدها بالكامل من مخزون المنزل المتوفر:",

        // Tasks Screen
        "tasks_tab" to "المهام",
        "task_title" to "عنوان المهمة",
        "task_desc" to "وصف المهمة",
        "assigned_to" to "مسندة إلى",
        "due_date" to "تاريخ الاستحقاق",
        "is_completed" to "حالة الإنجاز",
        "is_completed_yes" to "مكتملة",
        "is_completed_no" to "قيد التنفيذ",
        "add_task" to "إضافة مهمة جديدة",
        "recurrence" to "التكرار",
        "recur_none" to "بدون تكرار",
        "recur_daily" to "يومي",
        "recur_weekly" to "أسبوعي",
        "family_achievements" to "إنجازات العائلة والأوسمة",
        "completed_tasks" to "المهام المنجزة",

        // Chat & Communication
        "chat_tab" to "الدردشة والاتصال",
        "chat_placeholder" to "اكتب رسالة للعائلة...",
        "notes" to "الملاحظات المشتركة",
        "add_note" to "إضافة ملاحظة جديدة",
        "note_title" to "عنوان الملاحظة",
        "note_body" to "تفاصيل الملاحظة",

        // Expense Screen
        "expenses_tab" to "المالية",
        "expense_category" to "فئة المصروف",
        "amount" to "المبلغ",
        "add_expense" to "تسجيل مصروف جديد",
        "remaining_budget" to "المتبقي من الميزانية المحددة",
        "budget_limit" to "حد الميزانية الشهري",
        "spending_trends" to "توزيع المصروفات الشهري",
        "ai_budget_tips" to "تحليل وتوصيات ميزانية الذكاء الاصطناعي",

        // Profile, Settings, Notifications, About
        "profile_tab" to "الحساب",
        "edit_profile" to "تعديل الحساب",
        "logout" to "تسجيل الخروج",
        "settings_tab" to "الإعدادات",
        "dark_mode" to "المظهر الداكن (Dark Mode)",
        "language_toggle" to "اللغة / Language",
        "notifications_tab" to "التنبيهات",
        "clear_all" to "حذف الكل",
        "about_tab" to "حول التطبيق",
        "app_version" to "الإصدار الحالي",
        "contact_us" to "تواصل معنا",
        "privacy_policy" to "سياسة الخصوصية وشروط الاستخدام",

        // Categories
        "cat_food" to "مأكولات وأغذية",
        "cat_cleaning" to "منظفات ومستلزمات منزلية",
        "cat_personal" to "أغراض شخصية وصحة",
        "cat_other" to "أخرى",

        // Generic
        "save" to "حفظ",
        "cancel" to "إلغاء",
        "delete" to "حذف",
        "edit" to "تعديل",
        "success" to "تمت العملية بنجاح",
        "currency" to "ريال"
    )

    private val englishMap = mapOf(
        "app_name" to "Darna",
        "app_slogan" to "Smart Family & Household Management Platform",
        
        // Splash & Onboarding
        "get_started" to "Getting Started",
        "skip" to "Skip",
        "next" to "Next",
        "onboarding_1_title" to "Collaborative Shopping List",
        "onboarding_1_desc" to "Easily build lists and coordinate purchases with your family in real-time.",
        "onboarding_2_title" to "Smart Inventory Tracking",
        "onboarding_2_desc" to "Track ingredients, set expiry dates, and receive alerts before things run low.",
        "onboarding_3_title" to "AI-Driven Meal Planner",
        "onboarding_3_desc" to "Get creative recipes generated from your current kitchen store.",
        
        // Auth Flow
        "login_title" to "User Sign In",
        "register_title" to "Create Account",
        "forgot_password" to "Forgot Password?",
        "email_label" to "Email Address",
        "password_label" to "Password",
        "phone_label" to "Phone Number",
        "name_label" to "Full Name",
        "login_button" to "Sign In",
        "register_button" to "Sign Up",
        "google_login" to "Login with Google",
        "phone_login" to "Login with SMS OTP",
        "or_divider" to "Or through",
        "no_account" to "Don't have an account? Sign up",
        "has_account" to "Already registered? Login",
        "reset_password_btn" to "Send Reset Link",
        "reset_sent_msg" to "A password reset link has been dispatched to your email address.",
        "invalid_credentials" to "Invalid credentials. Please verify and retry.",

        // Family Setup
        "family_setup_title" to "Family Setup",
        "family_create_title" to "Create Fresh Joint Family",
        "family_join_title" to "Join an Existing Family",
        "family_name_label" to "Family Name (e.g. Al-Ahmad Family)",
        "invite_code_label" to "Family Invite Code",
        "create_family_btn" to "Create Family",
        "join_family_btn" to "Join Family",
        "enter_invite_code" to "Enter the 6-character Invite Code",
        "invite_code_helper" to "Share this invitation code so members can link with your store.",
        "invalid_invite" to "Invite code is invalid or expired. Check with admin.",

        // Dashboard Screen
        "dashboard_tab" to "Home",
        "welcome" to "Welcome, ",
        "role" to "Role",
        "admin" to "Family Owner",
        "member" to "Relative Member",
        "family_summary" to "My Household Summary",
        "shopping_summary" to "Pending Items",
        "inventory" to "Total Ingredients",
        "expiring_soon animate" to "Expiring Soon",
        "expiring_soon" to "Near Expiry",
        "low_stock" to "Low Stock",
        "upcoming_tasks" to "Assigned Tasks",
        "suggested_meals" to "Today's Meal Recommendation",
        "recent_activities" to "Recent Activity Feed",
        "monthly_expenses" to "Total Spent This Month",
        "quick_access" to "Functional Shortcuts",

        // Shopping List Screen
        "shopping_tab" to "Shopping",
        "add_item" to "Add Item",
        "category" to "Category",
        "quantity" to "Quantity",
        "unit" to "Unit",
        "purchased" to "Checked / Bought",
        "pending" to "Needed List",
        "added_by" to "Created by",
        "item_name_label" to "Product Name",
        "search_shopping" to "Search product lists...",
        "by_category" to "Filter Category",

        // Inventory Screen
        "inventory_tab" to "Kitchen Inventory",
        "expiry_date" to "Expiry Date",
        "add_product" to "Add Ingredient",
        "scan_barcode" to "Barcode Scanning",
        "scan_success" to "Barcode resolved successfully!",
        "product_details" to "Ingredient Properties",
        "threshold" to "Low Stock Threshold Alert",
        "expired" to "Expired",
        "unlimited" to "No Limit",

        // Meal Screen
        "meals_tab" to "Meals",
        "meal_planner" to "Weekly Meal Planner",
        "ai_suggestions" to "AI Generated Suggestions",
        "breakfast" to "Morning Breakfast",
        "lunch" to "Noon Lunch",
        "dinner" to "Evening Dinner",
        "add_meal" to "Add Planned Meal",
        "recipe_details" to "Detailed Instructions",
        "ingredients" to "Required Ingredients",
        "instructions" to "Cooking Methodology",
        "generate_ai_meals" to "Synthesize Recipes Using Gemini AI",
        "generating_ai" to "Scanning cupboards & crafting recipes...",
        "ai_results_desc" to "Nutritious recipes crafted directly from your current pantry:",

        // Tasks Screen
        "tasks_tab" to "Tasks",
        "task_title" to "Task Title",
        "task_desc" to "Task Description",
        "assigned_to" to "Assigned Relative",
        "due_date" to "Due Date",
        "is_completed" to "Status",
        "is_completed_yes" to "Completed",
        "is_completed_no" to "Pending Action",
        "add_task" to "Register Task",
        "recurrence" to "Recurrence Interval",
        "recur_none" to "No Recurrence",
        "recur_daily" to "Daily Recurrence",
        "recur_weekly" to "Weekly Recurrence",
        "family_achievements" to "Collective Badges & Awards",
        "completed_tasks" to "Completed family tasks",

        // Chat & Communication
        "chat_tab" to "Family Chat",
        "chat_placeholder" to "Type an update for your family...",
        "notes" to "Shared Notebook",
        "add_note" to "Create Note",
        "note_title" to "Note Title",
        "note_body" to "Markdown context text...",

        // Expense Screen
        "expenses_tab" to "Finance",
        "expense_category" to "Expense Category",
        "amount" to "Amount Sum",
        "add_expense" to "Record Expense",
        "remaining_budget" to "Remaining Budget Tracker",
        "budget_limit" to "Monthly Global Limit",
        "spending_trends" to "Category Expense Breakdown",
        "ai_budget_tips" to "Smart Cost Insights & Forecasts",

        // Profile, Settings, Notifications, About
        "profile_tab" to "Profile",
        "edit_profile" to "Modify Profile Details",
        "logout" to "Log Out Securely",
        "settings_tab" to "Settings",
        "dark_mode" to "Aesthetic Dark Theme",
        "language_toggle" to "Switch Languages",
        "notifications_tab" to "Inbox Alerts",
        "clear_all" to "Flush Notifications",
        "about_tab" to "About Darna",
        "app_version" to "Product Build Version",
        "contact_us" to "Support & Queries",
        "privacy_policy" to "Client Terms & Conditions",

        // Categories
        "cat_food" to "Grocery & Meals",
        "cat_cleaning" to "Hygiene & Detergents",
        "cat_personal" to "Personal Hygiene Products",
        "cat_other" to "Miscellaneous Goods",

        // Generic
        "save" to "Save Changes",
        "cancel" to "Cancel",
        "delete" to "Remove",
        "edit" to "Edit Form",
        "success" to "Information Synchronized!",
        "currency" to "SAR"
    )
}
