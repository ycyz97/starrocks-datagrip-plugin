// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes.*;
import static com.intellij.sql.dialects.base.SqlGeneratedParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class StarRocksGeneratedParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return script(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // "ADD" "COLUMN"? column_definition
  public static boolean add_column_action(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_column_action")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADD_COLUMN_ACTION, "<add column action>");
    result_ = consumeToken(builder_, "ADD");
    result_ = result_ && add_column_action_1(builder_, level_ + 1);
    result_ = result_ && column_definition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "COLUMN"?
  private static boolean add_column_action_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_column_action_1")) return false;
    consumeToken(builder_, "COLUMN");
    return true;
  }

  /* ********************************************************** */
  // multiplicative_expression (("+" | "-" | "||") multiplicative_expression)*
  public static boolean additive_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additive_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADDITIVE_EXPRESSION, "<additive expression>");
    result_ = multiplicative_expression(builder_, level_ + 1);
    result_ = result_ && additive_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (("+" | "-" | "||") multiplicative_expression)*
  private static boolean additive_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additive_expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!additive_expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "additive_expression_1", pos_)) break;
    }
    return true;
  }

  // ("+" | "-" | "||") multiplicative_expression
  private static boolean additive_expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additive_expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = additive_expression_1_0_0(builder_, level_ + 1);
    result_ = result_ && multiplicative_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "+" | "-" | "||"
  private static boolean additive_expression_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additive_expression_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "+");
    if (!result_) result_ = consumeToken(builder_, "-");
    if (!result_) result_ = consumeToken(builder_, "||");
    return result_;
  }

  /* ********************************************************** */
  // "ADMIN" "SHOW" ("FRONTEND" | "FRONTENDS" | "BACKEND" | "BACKENDS" | "BROKER" | "BROKERS") ("CONFIG" | "STATUS")?
  public static boolean admin_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "admin_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADMIN_STATEMENT, "<admin statement>");
    result_ = consumeToken(builder_, "ADMIN");
    result_ = result_ && consumeToken(builder_, "SHOW");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, admin_statement_2(builder_, level_ + 1));
    result_ = pinned_ && admin_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "FRONTEND" | "FRONTENDS" | "BACKEND" | "BACKENDS" | "BROKER" | "BROKERS"
  private static boolean admin_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "admin_statement_2")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "FRONTEND");
    if (!result_) result_ = consumeToken(builder_, "FRONTENDS");
    if (!result_) result_ = consumeToken(builder_, "BACKEND");
    if (!result_) result_ = consumeToken(builder_, "BACKENDS");
    if (!result_) result_ = consumeToken(builder_, "BROKER");
    if (!result_) result_ = consumeToken(builder_, "BROKERS");
    return result_;
  }

  // ("CONFIG" | "STATUS")?
  private static boolean admin_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "admin_statement_3")) return false;
    admin_statement_3_0(builder_, level_ + 1);
    return true;
  }

  // "CONFIG" | "STATUS"
  private static boolean admin_statement_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "admin_statement_3_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "CONFIG");
    if (!result_) result_ = consumeToken(builder_, "STATUS");
    return result_;
  }

  /* ********************************************************** */
  // "ALTER" "CATALOG" catalog_reference set_properties_action
  public static boolean alter_catalog_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_catalog_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_ALTER_CATALOG_STATEMENT, "<alter catalog statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "CATALOG");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, catalog_reference(builder_, level_ + 1));
    result_ = pinned_ && set_properties_action(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "ALTER" "MATERIALIZED" "VIEW" materialized_view_reference materialized_view_status? refresh_clause? properties_clause?
  public static boolean alter_materialized_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_materialized_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_MATERIALIZED_VIEW_STATEMENT, "<alter materialized view statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "MATERIALIZED");
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, materialized_view_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, alter_materialized_view_statement_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, alter_materialized_view_statement_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && alter_materialized_view_statement_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // materialized_view_status?
  private static boolean alter_materialized_view_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_materialized_view_statement_4")) return false;
    materialized_view_status(builder_, level_ + 1);
    return true;
  }

  // refresh_clause?
  private static boolean alter_materialized_view_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_materialized_view_statement_5")) return false;
    refresh_clause(builder_, level_ + 1);
    return true;
  }

  // properties_clause?
  private static boolean alter_materialized_view_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_materialized_view_statement_6")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // alter_user_statement | alter_role_statement
  public static boolean alter_principal_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_principal_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_PRINCIPAL_STATEMENT, "<alter principal statement>");
    result_ = alter_user_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_role_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "ALTER" "RESOURCE" resource_reference set_properties_action
  public static boolean alter_resource_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_resource_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_RESOURCE_STATEMENT, "<alter resource statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "RESOURCE");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, resource_reference(builder_, level_ + 1));
    result_ = pinned_ && set_properties_action(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "ALTER" "ROLE" security_principal principal_tail*
  public static boolean alter_role_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_role_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_ROLE_STATEMENT, "<alter role statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "ROLE");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, security_principal(builder_, level_ + 1));
    result_ = pinned_ && alter_role_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // principal_tail*
  private static boolean alter_role_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_role_statement_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!principal_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "alter_role_statement_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "ALTER" "ROUTINE" "LOAD" identifier_reference routine_load_clause*
  public static boolean alter_routine_load_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_routine_load_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_ROUTINE_LOAD_STATEMENT, "<alter routine load statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "ROUTINE");
    result_ = result_ && consumeToken(builder_, "LOAD");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, identifier_reference(builder_, level_ + 1));
    result_ = pinned_ && alter_routine_load_statement_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // routine_load_clause*
  private static boolean alter_routine_load_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_routine_load_statement_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!routine_load_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "alter_routine_load_statement_4", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "ALTER" ("DATABASE" | "SCHEMA") schema_reference set_properties_action
  public static boolean alter_schema_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_schema_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_ALTER_SCHEMA_STATEMENT, "<alter schema statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && alter_schema_statement_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, schema_reference(builder_, level_ + 1));
    result_ = pinned_ && set_properties_action(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "DATABASE" | "SCHEMA"
  private static boolean alter_schema_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_schema_statement_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "DATABASE");
    if (!result_) result_ = consumeToken(builder_, "SCHEMA");
    return result_;
  }

  /* ********************************************************** */
  // alter_table_statement
  //   | alter_view_statement
  //   | alter_materialized_view_statement
  //   | alter_catalog_statement
  //   | alter_resource_statement
  //   | alter_schema_statement
  //   | alter_principal_statement
  //   | alter_routine_load_statement
  public static boolean alter_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_STATEMENT, "<alter statement>");
    result_ = alter_table_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_view_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_materialized_view_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_catalog_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_resource_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_schema_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_principal_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_routine_load_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // add_column_action | modify_column_action | drop_column_action | rename_action | set_properties_action
  public static boolean alter_table_action(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_table_action")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_TABLE_ACTION, "<alter table action>");
    result_ = add_column_action(builder_, level_ + 1);
    if (!result_) result_ = modify_column_action(builder_, level_ + 1);
    if (!result_) result_ = drop_column_action(builder_, level_ + 1);
    if (!result_) result_ = rename_action(builder_, level_ + 1);
    if (!result_) result_ = set_properties_action(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "ALTER" "TABLE" if_exists? table_reference alter_table_action
  public static boolean alter_table_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_table_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_ALTER_TABLE_STATEMENT, "<alter table statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "TABLE");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, alter_table_statement_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, table_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && alter_table_action(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean alter_table_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_table_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "ALTER" "USER" security_principal principal_tail*
  public static boolean alter_user_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_user_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALTER_USER_STATEMENT, "<alter user statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "USER");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, security_principal(builder_, level_ + 1));
    result_ = pinned_ && alter_user_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // principal_tail*
  private static boolean alter_user_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_user_statement_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!principal_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "alter_user_statement_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "ALTER" "VIEW" view_reference as_select_clause
  public static boolean alter_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alter_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_ALTER_VIEW_STATEMENT, "<alter view statement>");
    result_ = consumeToken(builder_, "ALTER");
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, view_reference(builder_, level_ + 1));
    result_ = pinned_ && as_select_clause(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "OVER" (window_reference_name | "(" window_specification ")")
  public static boolean analytic_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analytic_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANALYTIC_CLAUSE, "<analytic clause>");
    result_ = consumeToken(builder_, "OVER");
    pinned_ = result_; // pin = 1
    result_ = result_ && analytic_clause_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::expression_recover);
    return result_ || pinned_;
  }

  // window_reference_name | "(" window_specification ")"
  private static boolean analytic_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analytic_clause_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = window_reference_name(builder_, level_ + 1);
    if (!result_) result_ = analytic_clause_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" window_specification ")"
  private static boolean analytic_clause_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analytic_clause_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "(");
    result_ = result_ && window_specification(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // parenthesized_identifier_list
  public static boolean analyze_column_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_column_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANALYZE_COLUMN_LIST, "<analyze column list>");
    result_ = parenthesized_identifier_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "UPDATE" "HISTOGRAM" "ON" column_reference ("," column_reference)*
  public static boolean analyze_histogram_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_histogram_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANALYZE_HISTOGRAM_CLAUSE, "<analyze histogram clause>");
    result_ = consumeToken(builder_, "UPDATE");
    result_ = result_ && consumeToken(builder_, "HISTOGRAM");
    result_ = result_ && consumeToken(builder_, "ON");
    result_ = result_ && column_reference(builder_, level_ + 1);
    result_ = result_ && analyze_histogram_clause_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," column_reference)*
  private static boolean analyze_histogram_clause_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_histogram_clause_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!analyze_histogram_clause_4_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "analyze_histogram_clause_4", pos_)) break;
    }
    return true;
  }

  // "," column_reference
  private static boolean analyze_histogram_clause_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_histogram_clause_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && column_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "ANALYZE" ("FULL" | "SAMPLE")? analyze_target analyze_histogram_clause? properties_clause?
  public static boolean analyze_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANALYZE_STATEMENT, "<analyze statement>");
    result_ = consumeToken(builder_, "ANALYZE");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, analyze_statement_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, analyze_target(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, analyze_statement_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && analyze_statement_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("FULL" | "SAMPLE")?
  private static boolean analyze_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_statement_1")) return false;
    analyze_statement_1_0(builder_, level_ + 1);
    return true;
  }

  // "FULL" | "SAMPLE"
  private static boolean analyze_statement_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_statement_1_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "FULL");
    if (!result_) result_ = consumeToken(builder_, "SAMPLE");
    return result_;
  }

  // analyze_histogram_clause?
  private static boolean analyze_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_statement_3")) return false;
    analyze_histogram_clause(builder_, level_ + 1);
    return true;
  }

  // properties_clause?
  private static boolean analyze_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_statement_4")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "TABLE" table_reference analyze_column_list?
  public static boolean analyze_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANALYZE_TARGET, "<analyze target>");
    result_ = consumeToken(builder_, "TABLE");
    result_ = result_ && table_reference(builder_, level_ + 1);
    result_ = result_ && analyze_target_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // analyze_column_list?
  private static boolean analyze_target_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "analyze_target_2")) return false;
    analyze_column_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // not_expression ("AND" not_expression)*
  public static boolean and_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, AND_EXPRESSION, "<and expression>");
    result_ = not_expression(builder_, level_ + 1);
    result_ = result_ && and_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("AND" not_expression)*
  private static boolean and_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!and_expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "and_expression_1", pos_)) break;
    }
    return true;
  }

  // "AND" not_expression
  private static boolean and_expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "AND");
    result_ = result_ && not_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "[" value_expression "]"
  public static boolean array_access_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_access_tail")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARRAY_ACCESS_TAIL, "<array access tail>");
    result_ = consumeToken(builder_, "[");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "]");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "ARRAY" "<" type_element ">"
  public static boolean array_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARRAY_TYPE, "<array type>");
    result_ = consumeToken(builder_, "ARRAY");
    result_ = result_ && consumeToken(builder_, "<");
    result_ = result_ && type_element(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ">");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "AS" query_expression
  public static boolean as_select_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "as_select_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, AS_SELECT_CLAUSE, "<as select clause>");
    result_ = consumeToken(builder_, "AS");
    pinned_ = result_; // pin = 1
    result_ = result_ && query_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ("BACKUP" | "RESTORE") "SNAPSHOT" identifier_reference ("TO" | "FROM") identifier_reference ("ON" "(" table_reference ("," table_reference)* ")")? properties_clause?
  //   | "RECOVER" ("TABLE" | "DATABASE" | "PARTITION" | "REPOSITORY")? identifier_reference
  public static boolean backup_restore_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BACKUP_RESTORE_STATEMENT, "<backup restore statement>");
    result_ = backup_restore_statement_0(builder_, level_ + 1);
    if (!result_) result_ = backup_restore_statement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::statement_recover);
    return result_;
  }

  // ("BACKUP" | "RESTORE") "SNAPSHOT" identifier_reference ("TO" | "FROM") identifier_reference ("ON" "(" table_reference ("," table_reference)* ")")? properties_clause?
  private static boolean backup_restore_statement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = backup_restore_statement_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "SNAPSHOT");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    result_ = result_ && backup_restore_statement_0_3(builder_, level_ + 1);
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    result_ = result_ && backup_restore_statement_0_5(builder_, level_ + 1);
    result_ = result_ && backup_restore_statement_0_6(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "BACKUP" | "RESTORE"
  private static boolean backup_restore_statement_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "BACKUP");
    if (!result_) result_ = consumeToken(builder_, "RESTORE");
    return result_;
  }

  // "TO" | "FROM"
  private static boolean backup_restore_statement_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_3")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "TO");
    if (!result_) result_ = consumeToken(builder_, "FROM");
    return result_;
  }

  // ("ON" "(" table_reference ("," table_reference)* ")")?
  private static boolean backup_restore_statement_0_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_5")) return false;
    backup_restore_statement_0_5_0(builder_, level_ + 1);
    return true;
  }

  // "ON" "(" table_reference ("," table_reference)* ")"
  private static boolean backup_restore_statement_0_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "ON");
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && table_reference(builder_, level_ + 1);
    result_ = result_ && backup_restore_statement_0_5_0_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("," table_reference)*
  private static boolean backup_restore_statement_0_5_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_5_0_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!backup_restore_statement_0_5_0_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "backup_restore_statement_0_5_0_3", pos_)) break;
    }
    return true;
  }

  // "," table_reference
  private static boolean backup_restore_statement_0_5_0_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_5_0_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && table_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // properties_clause?
  private static boolean backup_restore_statement_0_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_0_6")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  // "RECOVER" ("TABLE" | "DATABASE" | "PARTITION" | "REPOSITORY")? identifier_reference
  private static boolean backup_restore_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "RECOVER");
    result_ = result_ && backup_restore_statement_1_1(builder_, level_ + 1);
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("TABLE" | "DATABASE" | "PARTITION" | "REPOSITORY")?
  private static boolean backup_restore_statement_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_1_1")) return false;
    backup_restore_statement_1_1_0(builder_, level_ + 1);
    return true;
  }

  // "TABLE" | "DATABASE" | "PARTITION" | "REPOSITORY"
  private static boolean backup_restore_statement_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backup_restore_statement_1_1_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "TABLE");
    if (!result_) result_ = consumeToken(builder_, "DATABASE");
    if (!result_) result_ = consumeToken(builder_, "PARTITION");
    if (!result_) result_ = consumeToken(builder_, "REPOSITORY");
    return result_;
  }

  /* ********************************************************** */
  // "BEGIN" | "START" "TRANSACTION"
  public static boolean begin_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "begin_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_START_TRANSACTION_STATEMENT, "<begin statement>");
    result_ = consumeToken(builder_, "BEGIN");
    if (!result_) result_ = begin_statement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "START" "TRANSACTION"
  private static boolean begin_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "begin_statement_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "START");
    result_ = result_ && consumeToken(builder_, "TRANSACTION");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "BUCKETS" numeric_literal
  public static boolean buckets_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "buckets_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BUCKETS_CLAUSE, "<buckets clause>");
    result_ = consumeToken(builder_, "BUCKETS");
    pinned_ = result_; // pin = 1
    result_ = result_ && numeric_literal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "CALL" function_call
  public static boolean call_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "call_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CALL_STATEMENT, "<call statement>");
    result_ = consumeToken(builder_, "CALL");
    pinned_ = result_; // pin = 1
    result_ = result_ && function_call(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "CANCEL" "LOAD" ("FROM" schema_reference)? where_clause?
  public static boolean cancel_load_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cancel_load_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CANCEL_LOAD_STATEMENT, "<cancel load statement>");
    result_ = consumeToken(builder_, "CANCEL");
    result_ = result_ && consumeToken(builder_, "LOAD");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, cancel_load_statement_2(builder_, level_ + 1));
    result_ = pinned_ && cancel_load_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("FROM" schema_reference)?
  private static boolean cancel_load_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cancel_load_statement_2")) return false;
    cancel_load_statement_2_0(builder_, level_ + 1);
    return true;
  }

  // "FROM" schema_reference
  private static boolean cancel_load_statement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cancel_load_statement_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "FROM");
    result_ = result_ && schema_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // where_clause?
  private static boolean cancel_load_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cancel_load_statement_3")) return false;
    where_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "ELSE" value_expression
  public static boolean case_else_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_else_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CASE_ELSE_CLAUSE, "<case else clause>");
    result_ = consumeToken(builder_, "ELSE");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CASE" value_expression? case_when_clause+ case_else_clause? "END"
  public static boolean case_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_expression")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CASE_EXPRESSION, "<case expression>");
    result_ = consumeToken(builder_, "CASE");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, case_expression_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, case_expression_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, case_expression_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, "END") && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::expression_recover);
    return result_ || pinned_;
  }

  // value_expression?
  private static boolean case_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_expression_1")) return false;
    value_expression(builder_, level_ + 1);
    return true;
  }

  // case_when_clause+
  private static boolean case_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_expression_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = case_when_clause(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!case_when_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "case_expression_2", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // case_else_clause?
  private static boolean case_expression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_expression_3")) return false;
    case_else_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "WHEN" value_expression "THEN" value_expression
  public static boolean case_when_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_when_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CASE_WHEN_CLAUSE, "<case when clause>");
    result_ = consumeToken(builder_, "WHEN");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "THEN");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CAST" "(" value_expression "AS" cast_type ")"
  public static boolean cast_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cast_expression")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CAST_EXPRESSION, "<cast expression>");
    result_ = consumeToken(builder_, "CAST");
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && value_expression(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, consumeToken(builder_, "AS"));
    result_ = pinned_ && report_error_(builder_, cast_type(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ")") && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::expression_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // type_element
  public static boolean cast_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cast_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CAST_TYPE, "<cast type>");
    result_ = type_element(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::type_recover);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean catalog_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "catalog_reference")) return false;
    if (!nextTokenIs(builder_, "<catalog reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CATALOG_REFERENCE, "<catalog reference>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "NOT" "NULL" | "NULL" | "DEFAULT" value_expression | "COMMENT" string_literal | key_model "KEY" | "AUTO_INCREMENT"
  public static boolean column_attribute(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_attribute")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COLUMN_ATTRIBUTE, "<column attribute>");
    result_ = column_attribute_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "NULL");
    if (!result_) result_ = column_attribute_2(builder_, level_ + 1);
    if (!result_) result_ = column_attribute_3(builder_, level_ + 1);
    if (!result_) result_ = column_attribute_4(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "AUTO_INCREMENT");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "NOT" "NULL"
  private static boolean column_attribute_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_attribute_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "NOT");
    result_ = result_ && consumeToken(builder_, "NULL");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "DEFAULT" value_expression
  private static boolean column_attribute_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_attribute_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "DEFAULT");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "COMMENT" string_literal
  private static boolean column_attribute_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_attribute_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "COMMENT");
    result_ = result_ && string_literal(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // key_model "KEY"
  private static boolean column_attribute_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_attribute_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = key_model(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "KEY");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // column_name type_element column_attribute*
  public static boolean column_definition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_definition")) return false;
    if (!nextTokenIs(builder_, "<column definition>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_COLUMN_DEFINITION, "<column definition>");
    result_ = column_name(builder_, level_ + 1);
    result_ = result_ && type_element(builder_, level_ + 1);
    result_ = result_ && column_definition_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // column_attribute*
  private static boolean column_definition_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_definition_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!column_attribute(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "column_definition_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean column_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_name")) return false;
    if (!nextTokenIs(builder_, "<column name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COLUMN_NAME, "<column name>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // qualified_column_prefix? column_reference_name
  public static boolean column_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_reference")) return false;
    if (!nextTokenIs(builder_, "<column reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COLUMN_REFERENCE, "<column reference>");
    result_ = column_reference_0(builder_, level_ + 1);
    result_ = result_ && column_reference_name(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // qualified_column_prefix?
  private static boolean column_reference_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_reference_0")) return false;
    qualified_column_prefix(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean column_reference_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "column_reference_name")) return false;
    if (!nextTokenIs(builder_, "<column reference name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COLUMN_REFERENCE_NAME, "<column reference name>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "COLUMNS" ("TERMINATED" "BY" string_literal)?
  public static boolean columns_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "columns_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COLUMNS_CLAUSE, "<columns clause>");
    result_ = consumeToken(builder_, "COLUMNS");
    result_ = result_ && columns_clause_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("TERMINATED" "BY" string_literal)?
  private static boolean columns_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "columns_clause_1")) return false;
    columns_clause_1_0(builder_, level_ + 1);
    return true;
  }

  // "TERMINATED" "BY" string_literal
  private static boolean columns_clause_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "columns_clause_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "TERMINATED");
    result_ = result_ && consumeToken(builder_, "BY");
    result_ = result_ && string_literal(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "COMMENT" string_literal
  public static boolean comment_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comment_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMMENT_CLAUSE, "<comment clause>");
    result_ = consumeToken(builder_, "COMMENT");
    pinned_ = result_; // pin = 1
    result_ = result_ && string_literal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "COMMIT"
  public static boolean commit_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "commit_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_COMMIT_STATEMENT, "<commit statement>");
    result_ = consumeToken(builder_, "COMMIT");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // additive_expression (comparison_operator additive_expression | "BETWEEN" additive_expression "AND" additive_expression | ("NOT"? "IN") in_list_or_subquery)*
  public static boolean comparison_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPARISON_EXPRESSION, "<comparison expression>");
    result_ = additive_expression(builder_, level_ + 1);
    result_ = result_ && comparison_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (comparison_operator additive_expression | "BETWEEN" additive_expression "AND" additive_expression | ("NOT"? "IN") in_list_or_subquery)*
  private static boolean comparison_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comparison_expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "comparison_expression_1", pos_)) break;
    }
    return true;
  }

  // comparison_operator additive_expression | "BETWEEN" additive_expression "AND" additive_expression | ("NOT"? "IN") in_list_or_subquery
  private static boolean comparison_expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = comparison_expression_1_0_0(builder_, level_ + 1);
    if (!result_) result_ = comparison_expression_1_0_1(builder_, level_ + 1);
    if (!result_) result_ = comparison_expression_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // comparison_operator additive_expression
  private static boolean comparison_expression_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = comparison_operator(builder_, level_ + 1);
    result_ = result_ && additive_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "BETWEEN" additive_expression "AND" additive_expression
  private static boolean comparison_expression_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "BETWEEN");
    result_ = result_ && additive_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "AND");
    result_ = result_ && additive_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("NOT"? "IN") in_list_or_subquery
  private static boolean comparison_expression_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1_0_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = comparison_expression_1_0_2_0(builder_, level_ + 1);
    result_ = result_ && in_list_or_subquery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "NOT"? "IN"
  private static boolean comparison_expression_1_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = comparison_expression_1_0_2_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "IN");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "NOT"?
  private static boolean comparison_expression_1_0_2_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expression_1_0_2_0_0")) return false;
    consumeToken(builder_, "NOT");
    return true;
  }

  /* ********************************************************** */
  // "=" | "<" | ">" | "<=" | ">=" | "<>" | "!=" | "<=>" | "LIKE" | "REGEXP" | "RLIKE" | "IS" "NOT"?
  static boolean comparison_operator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_operator")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "=");
    if (!result_) result_ = consumeToken(builder_, "<");
    if (!result_) result_ = consumeToken(builder_, ">");
    if (!result_) result_ = consumeToken(builder_, "<=");
    if (!result_) result_ = consumeToken(builder_, ">=");
    if (!result_) result_ = consumeToken(builder_, "<>");
    if (!result_) result_ = consumeToken(builder_, "!=");
    if (!result_) result_ = consumeToken(builder_, "<=>");
    if (!result_) result_ = consumeToken(builder_, "LIKE");
    if (!result_) result_ = consumeToken(builder_, "REGEXP");
    if (!result_) result_ = consumeToken(builder_, "RLIKE");
    if (!result_) result_ = comparison_operator_11(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "IS" "NOT"?
  private static boolean comparison_operator_11(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_operator_11")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "IS");
    result_ = result_ && comparison_operator_11_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "NOT"?
  private static boolean comparison_operator_11_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_operator_11_1")) return false;
    consumeToken(builder_, "NOT");
    return true;
  }

  /* ********************************************************** */
  // array_type | map_type | struct_type
  public static boolean complex_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "complex_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPLEX_TYPE, "<complex type>");
    result_ = array_type(builder_, level_ + 1);
    if (!result_) result_ = map_type(builder_, level_ + 1);
    if (!result_) result_ = struct_type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CREATE" "EXTERNAL"? "CATALOG" catalog_reference properties_clause? comment_clause?
  public static boolean create_catalog_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_catalog_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CREATE_CATALOG_STATEMENT, "<create catalog statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && create_catalog_statement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "CATALOG");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, catalog_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, create_catalog_statement_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_catalog_statement_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "EXTERNAL"?
  private static boolean create_catalog_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_catalog_statement_1")) return false;
    consumeToken(builder_, "EXTERNAL");
    return true;
  }

  // properties_clause?
  private static boolean create_catalog_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_catalog_statement_4")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  // comment_clause?
  private static boolean create_catalog_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_catalog_statement_5")) return false;
    comment_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "CREATE" "BITMAP"? "INDEX" index_reference "ON" table_reference parenthesized_identifier_list properties_clause? comment_clause?
  public static boolean create_index_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_index_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CREATE_INDEX_STATEMENT, "<create index statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && create_index_statement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "INDEX");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, index_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "ON")) && result_;
    result_ = pinned_ && report_error_(builder_, table_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, parenthesized_identifier_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_index_statement_7(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_index_statement_8(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "BITMAP"?
  private static boolean create_index_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_index_statement_1")) return false;
    consumeToken(builder_, "BITMAP");
    return true;
  }

  // properties_clause?
  private static boolean create_index_statement_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_index_statement_7")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  // comment_clause?
  private static boolean create_index_statement_8(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_index_statement_8")) return false;
    comment_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // partition_clause | distribution_clause | buckets_clause | refresh_clause | comment_clause | properties_clause
  public static boolean create_materialized_view_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_materialized_view_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_MATERIALIZED_VIEW_CLAUSE, "<create materialized view clause>");
    result_ = partition_clause(builder_, level_ + 1);
    if (!result_) result_ = distribution_clause(builder_, level_ + 1);
    if (!result_) result_ = buckets_clause(builder_, level_ + 1);
    if (!result_) result_ = refresh_clause(builder_, level_ + 1);
    if (!result_) result_ = comment_clause(builder_, level_ + 1);
    if (!result_) result_ = properties_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CREATE" "MATERIALIZED" "VIEW" if_not_exists? materialized_view_reference table_column_list? create_materialized_view_clause* as_select_clause
  public static boolean create_materialized_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_materialized_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CREATE_MATERIALIZED_VIEW_STATEMENT, "<create materialized view statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && consumeToken(builder_, "MATERIALIZED");
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, create_materialized_view_statement_3(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, materialized_view_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_materialized_view_statement_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_materialized_view_statement_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && as_select_clause(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_not_exists?
  private static boolean create_materialized_view_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_materialized_view_statement_3")) return false;
    if_not_exists(builder_, level_ + 1);
    return true;
  }

  // table_column_list?
  private static boolean create_materialized_view_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_materialized_view_statement_5")) return false;
    table_column_list(builder_, level_ + 1);
    return true;
  }

  // create_materialized_view_clause*
  private static boolean create_materialized_view_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_materialized_view_statement_6")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!create_materialized_view_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "create_materialized_view_statement_6", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // create_user_statement | create_role_statement
  public static boolean create_principal_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_principal_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_PRINCIPAL_STATEMENT, "<create principal statement>");
    result_ = create_user_statement(builder_, level_ + 1);
    if (!result_) result_ = create_role_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CREATE" "REPOSITORY" identifier_reference "WITH" "BROKER" identifier_reference? properties_clause?
  public static boolean create_repository_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_repository_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_REPOSITORY_STATEMENT, "<create repository statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && consumeToken(builder_, "REPOSITORY");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "WITH")) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "BROKER")) && result_;
    result_ = pinned_ && report_error_(builder_, create_repository_statement_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_repository_statement_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // identifier_reference?
  private static boolean create_repository_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_repository_statement_5")) return false;
    identifier_reference(builder_, level_ + 1);
    return true;
  }

  // properties_clause?
  private static boolean create_repository_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_repository_statement_6")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "CREATE" "EXTERNAL"? "RESOURCE" resource_reference properties_clause?
  public static boolean create_resource_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_resource_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_RESOURCE_STATEMENT, "<create resource statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && create_resource_statement_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, consumeToken(builder_, "RESOURCE"));
    result_ = pinned_ && report_error_(builder_, resource_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_resource_statement_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "EXTERNAL"?
  private static boolean create_resource_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_resource_statement_1")) return false;
    consumeToken(builder_, "EXTERNAL");
    return true;
  }

  // properties_clause?
  private static boolean create_resource_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_resource_statement_4")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "CREATE" "ROLE" security_principal principal_tail*
  public static boolean create_role_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_role_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_ROLE_STATEMENT, "<create role statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && consumeToken(builder_, "ROLE");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, security_principal(builder_, level_ + 1));
    result_ = pinned_ && create_role_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // principal_tail*
  private static boolean create_role_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_role_statement_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!principal_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "create_role_statement_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "CREATE" "ROUTINE" "LOAD" identifier_reference "ON" table_reference routine_load_clause*
  public static boolean create_routine_load_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_routine_load_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_ROUTINE_LOAD_STATEMENT, "<create routine load statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && consumeToken(builder_, "ROUTINE");
    result_ = result_ && consumeToken(builder_, "LOAD");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, identifier_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "ON")) && result_;
    result_ = pinned_ && report_error_(builder_, table_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_routine_load_statement_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // routine_load_clause*
  private static boolean create_routine_load_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_routine_load_statement_6")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!routine_load_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "create_routine_load_statement_6", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "CREATE" ("DATABASE" | "SCHEMA") if_not_exists? schema_reference properties_clause? comment_clause?
  public static boolean create_schema_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_schema_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CREATE_SCHEMA_STATEMENT, "<create schema statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && create_schema_statement_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, create_schema_statement_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, schema_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_schema_statement_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_schema_statement_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "DATABASE" | "SCHEMA"
  private static boolean create_schema_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_schema_statement_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "DATABASE");
    if (!result_) result_ = consumeToken(builder_, "SCHEMA");
    return result_;
  }

  // if_not_exists?
  private static boolean create_schema_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_schema_statement_2")) return false;
    if_not_exists(builder_, level_ + 1);
    return true;
  }

  // properties_clause?
  private static boolean create_schema_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_schema_statement_4")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  // comment_clause?
  private static boolean create_schema_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_schema_statement_5")) return false;
    comment_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // create_table_statement
  //   | create_view_statement
  //   | create_materialized_view_statement
  //   | create_catalog_statement
  //   | create_resource_statement
  //   | create_routine_load_statement
  //   | create_repository_statement
  //   | create_principal_statement
  //   | create_schema_statement
  //   | create_index_statement
  public static boolean create_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_STATEMENT, "<create statement>");
    result_ = create_table_statement(builder_, level_ + 1);
    if (!result_) result_ = create_view_statement(builder_, level_ + 1);
    if (!result_) result_ = create_materialized_view_statement(builder_, level_ + 1);
    if (!result_) result_ = create_catalog_statement(builder_, level_ + 1);
    if (!result_) result_ = create_resource_statement(builder_, level_ + 1);
    if (!result_) result_ = create_routine_load_statement(builder_, level_ + 1);
    if (!result_) result_ = create_repository_statement(builder_, level_ + 1);
    if (!result_) result_ = create_principal_statement(builder_, level_ + 1);
    if (!result_) result_ = create_schema_statement(builder_, level_ + 1);
    if (!result_) result_ = create_index_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // key_model_clause
  //   | partition_clause
  //   | distribution_clause
  //   | buckets_clause
  //   | comment_clause
  //   | properties_clause
  //   | order_by_clause
  public static boolean create_table_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_TABLE_CLAUSE, "<create table clause>");
    result_ = key_model_clause(builder_, level_ + 1);
    if (!result_) result_ = partition_clause(builder_, level_ + 1);
    if (!result_) result_ = distribution_clause(builder_, level_ + 1);
    if (!result_) result_ = buckets_clause(builder_, level_ + 1);
    if (!result_) result_ = comment_clause(builder_, level_ + 1);
    if (!result_) result_ = properties_clause(builder_, level_ + 1);
    if (!result_) result_ = order_by_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CREATE" table_scope? "TABLE" if_not_exists? table_reference table_column_list? create_table_clause* as_select_clause?
  public static boolean create_table_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CREATE_TABLE_STATEMENT, "<create table statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && create_table_statement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "TABLE");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, create_table_statement_3(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, table_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_table_statement_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_table_statement_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && create_table_statement_7(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // table_scope?
  private static boolean create_table_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_statement_1")) return false;
    table_scope(builder_, level_ + 1);
    return true;
  }

  // if_not_exists?
  private static boolean create_table_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_statement_3")) return false;
    if_not_exists(builder_, level_ + 1);
    return true;
  }

  // table_column_list?
  private static boolean create_table_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_statement_5")) return false;
    table_column_list(builder_, level_ + 1);
    return true;
  }

  // create_table_clause*
  private static boolean create_table_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_statement_6")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!create_table_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "create_table_statement_6", pos_)) break;
    }
    return true;
  }

  // as_select_clause?
  private static boolean create_table_statement_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_table_statement_7")) return false;
    as_select_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "CREATE" "USER" security_principal principal_tail*
  public static boolean create_user_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_user_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CREATE_USER_STATEMENT, "<create user statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && consumeToken(builder_, "USER");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, security_principal(builder_, level_ + 1));
    result_ = pinned_ && create_user_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // principal_tail*
  private static boolean create_user_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_user_statement_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!principal_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "create_user_statement_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "CREATE" ("OR" "REPLACE")? "VIEW" if_not_exists? view_reference table_column_list? comment_clause? as_select_clause
  public static boolean create_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_CREATE_VIEW_STATEMENT, "<create view statement>");
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && create_view_statement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, create_view_statement_3(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, view_reference(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_view_statement_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, create_view_statement_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && as_select_clause(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("OR" "REPLACE")?
  private static boolean create_view_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_view_statement_1")) return false;
    create_view_statement_1_0(builder_, level_ + 1);
    return true;
  }

  // "OR" "REPLACE"
  private static boolean create_view_statement_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_view_statement_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "OR");
    result_ = result_ && consumeToken(builder_, "REPLACE");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // if_not_exists?
  private static boolean create_view_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_view_statement_3")) return false;
    if_not_exists(builder_, level_ + 1);
    return true;
  }

  // table_column_list?
  private static boolean create_view_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_view_statement_5")) return false;
    table_column_list(builder_, level_ + 1);
    return true;
  }

  // comment_clause?
  private static boolean create_view_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "create_view_statement_6")) return false;
    comment_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "(" cte_column_name ("," cte_column_name)* ","? ")"
  public static boolean cte_column_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cte_column_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CTE_COLUMN_LIST, "<cte column list>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && cte_column_name(builder_, level_ + 1);
    result_ = result_ && cte_column_list_2(builder_, level_ + 1);
    result_ = result_ && cte_column_list_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," cte_column_name)*
  private static boolean cte_column_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cte_column_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!cte_column_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "cte_column_list_2", pos_)) break;
    }
    return true;
  }

  // "," cte_column_name
  private static boolean cte_column_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cte_column_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && cte_column_name(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ","?
  private static boolean cte_column_list_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cte_column_list_3")) return false;
    consumeToken(builder_, ",");
    return true;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean cte_column_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cte_column_name")) return false;
    if (!nextTokenIs(builder_, "<cte column name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CTE_COLUMN_NAME, "<cte column name>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // create_statement | alter_statement | drop_statement | truncate_table_statement | refresh_materialized_view_statement | grant_statement | revoke_statement
  public static boolean ddl_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ddl_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DDL_STATEMENT, "<ddl statement>");
    result_ = create_statement(builder_, level_ + 1);
    if (!result_) result_ = alter_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_statement(builder_, level_ + 1);
    if (!result_) result_ = truncate_table_statement(builder_, level_ + 1);
    if (!result_) result_ = refresh_materialized_view_statement(builder_, level_ + 1);
    if (!result_) result_ = grant_statement(builder_, level_ + 1);
    if (!result_) result_ = revoke_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "CREATE" | "ALTER" | "DROP" | "TRUNCATE" | "REFRESH" | "GRANT" | "REVOKE"
  static boolean ddl_statement_start(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ddl_statement_start")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "CREATE");
    if (!result_) result_ = consumeToken(builder_, "ALTER");
    if (!result_) result_ = consumeToken(builder_, "DROP");
    if (!result_) result_ = consumeToken(builder_, "TRUNCATE");
    if (!result_) result_ = consumeToken(builder_, "REFRESH");
    if (!result_) result_ = consumeToken(builder_, "GRANT");
    if (!result_) result_ = consumeToken(builder_, "REVOKE");
    return result_;
  }

  /* ********************************************************** */
  // "DEFAULT" "ROLE" security_principal_list
  public static boolean default_role_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "default_role_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DEFAULT_ROLE_CLAUSE, "<default role clause>");
    result_ = consumeToken(builder_, "DEFAULT");
    result_ = result_ && consumeToken(builder_, "ROLE");
    result_ = result_ && security_principal_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "DELETE" "FROM"? dml_target_table where_clause?
  public static boolean delete_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "delete_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_DELETE_STATEMENT, "<delete statement>");
    result_ = consumeToken(builder_, "DELETE");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, delete_statement_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, dml_target_table(builder_, level_ + 1)) && result_;
    result_ = pinned_ && delete_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "FROM"?
  private static boolean delete_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "delete_statement_1")) return false;
    consumeToken(builder_, "FROM");
    return true;
  }

  // where_clause?
  private static boolean delete_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "delete_statement_3")) return false;
    where_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // SQL_IDENT_DELIMITED
  public static boolean delimited_identifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "delimited_identifier")) return false;
    if (!nextTokenIs(builder_, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SQL_IDENT_DELIMITED);
    exit_section_(builder_, marker_, DELIMITED_IDENTIFIER, result_);
    return result_;
  }

  /* ********************************************************** */
  // ("DESC" | "DESCRIBE") "TABLE"? describe_target column_reference?
  public static boolean describe_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "describe_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DESCRIBE_STATEMENT, "<describe statement>");
    result_ = describe_statement_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, describe_statement_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, describe_target(builder_, level_ + 1)) && result_;
    result_ = pinned_ && describe_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "DESC" | "DESCRIBE"
  private static boolean describe_statement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "describe_statement_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "DESC");
    if (!result_) result_ = consumeToken(builder_, "DESCRIBE");
    return result_;
  }

  // "TABLE"?
  private static boolean describe_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "describe_statement_1")) return false;
    consumeToken(builder_, "TABLE");
    return true;
  }

  // column_reference?
  private static boolean describe_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "describe_statement_3")) return false;
    column_reference(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // table_reference
  public static boolean describe_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "describe_target")) return false;
    if (!nextTokenIs(builder_, "<describe target>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DESCRIBE_TARGET, "<describe target>");
    result_ = table_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "DISTRIBUTED" "BY" distribution_expression buckets_clause?
  public static boolean distribution_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "distribution_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DISTRIBUTION_CLAUSE, "<distribution clause>");
    result_ = consumeToken(builder_, "DISTRIBUTED");
    result_ = result_ && consumeToken(builder_, "BY");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, distribution_expression(builder_, level_ + 1));
    result_ = pinned_ && distribution_clause_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // buckets_clause?
  private static boolean distribution_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "distribution_clause_3")) return false;
    buckets_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "HASH" parenthesized_identifier_list | "RANDOM" | function_call
  public static boolean distribution_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "distribution_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DISTRIBUTION_EXPRESSION, "<distribution expression>");
    result_ = distribution_expression_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "RANDOM");
    if (!result_) result_ = function_call(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "HASH" parenthesized_identifier_list
  private static boolean distribution_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "distribution_expression_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "HASH");
    result_ = result_ && parenthesized_identifier_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // insert_statement | update_statement | delete_statement | merge_statement | select_statement
  public static boolean dml_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dml_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DML_STATEMENT, "<dml statement>");
    result_ = insert_statement(builder_, level_ + 1);
    if (!result_) result_ = update_statement(builder_, level_ + 1);
    if (!result_) result_ = delete_statement(builder_, level_ + 1);
    if (!result_) result_ = merge_statement(builder_, level_ + 1);
    if (!result_) result_ = select_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "SELECT" | "WITH" | "VALUES" | "INSERT" | "UPDATE" | "DELETE" | "MERGE"
  static boolean dml_statement_start(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dml_statement_start")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "SELECT");
    if (!result_) result_ = consumeToken(builder_, "WITH");
    if (!result_) result_ = consumeToken(builder_, "VALUES");
    if (!result_) result_ = consumeToken(builder_, "INSERT");
    if (!result_) result_ = consumeToken(builder_, "UPDATE");
    if (!result_) result_ = consumeToken(builder_, "DELETE");
    if (!result_) result_ = consumeToken(builder_, "MERGE");
    return result_;
  }

  /* ********************************************************** */
  // table_reference table_alias_clause?
  public static boolean dml_target_table(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dml_target_table")) return false;
    if (!nextTokenIs(builder_, "<dml target table>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DML_TARGET_TABLE, "<dml target table>");
    result_ = table_reference(builder_, level_ + 1);
    result_ = result_ && dml_target_table_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // table_alias_clause?
  private static boolean dml_target_table_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dml_target_table_1")) return false;
    table_alias_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "DROP" "CATALOG" if_exists? catalog_reference
  public static boolean drop_catalog_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_catalog_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_CATALOG_STATEMENT, "<drop catalog statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "CATALOG");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, drop_catalog_statement_2(builder_, level_ + 1));
    result_ = pinned_ && catalog_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean drop_catalog_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_catalog_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "DROP" "COLUMN"? identifier_reference
  public static boolean drop_column_action(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_column_action")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_COLUMN_ACTION, "<drop column action>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && drop_column_action_1(builder_, level_ + 1);
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "COLUMN"?
  private static boolean drop_column_action_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_column_action_1")) return false;
    consumeToken(builder_, "COLUMN");
    return true;
  }

  /* ********************************************************** */
  // "DROP" "INDEX" index_reference "ON" table_reference
  public static boolean drop_index_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_index_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_INDEX_STATEMENT, "<drop index statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "INDEX");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, index_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "ON")) && result_;
    result_ = pinned_ && table_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "DROP" "MATERIALIZED" "VIEW" if_exists? materialized_view_reference
  public static boolean drop_materialized_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_materialized_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_MATERIALIZED_VIEW_STATEMENT, "<drop materialized view statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "MATERIALIZED");
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, drop_materialized_view_statement_3(builder_, level_ + 1));
    result_ = pinned_ && materialized_view_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean drop_materialized_view_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_materialized_view_statement_3")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // drop_user_statement | drop_role_statement
  public static boolean drop_principal_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_principal_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_PRINCIPAL_STATEMENT, "<drop principal statement>");
    result_ = drop_user_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_role_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "DROP" "REPOSITORY" if_exists? identifier_reference
  public static boolean drop_repository_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_repository_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_REPOSITORY_STATEMENT, "<drop repository statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "REPOSITORY");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, drop_repository_statement_2(builder_, level_ + 1));
    result_ = pinned_ && identifier_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean drop_repository_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_repository_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "DROP" "RESOURCE" if_exists? resource_reference
  public static boolean drop_resource_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_resource_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_RESOURCE_STATEMENT, "<drop resource statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "RESOURCE");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, drop_resource_statement_2(builder_, level_ + 1));
    result_ = pinned_ && resource_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean drop_resource_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_resource_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "DROP" "ROLE" security_principal
  public static boolean drop_role_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_role_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_ROLE_STATEMENT, "<drop role statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "ROLE");
    pinned_ = result_; // pin = 2
    result_ = result_ && security_principal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "DROP" ("DATABASE" | "SCHEMA") if_exists? schema_reference
  public static boolean drop_schema_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_schema_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_SCHEMA_STATEMENT, "<drop schema statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && drop_schema_statement_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, drop_schema_statement_2(builder_, level_ + 1));
    result_ = pinned_ && schema_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "DATABASE" | "SCHEMA"
  private static boolean drop_schema_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_schema_statement_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "DATABASE");
    if (!result_) result_ = consumeToken(builder_, "SCHEMA");
    return result_;
  }

  // if_exists?
  private static boolean drop_schema_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_schema_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // drop_table_statement
  //   | drop_view_statement
  //   | drop_materialized_view_statement
  //   | drop_catalog_statement
  //   | drop_resource_statement
  //   | drop_repository_statement
  //   | drop_schema_statement
  //   | drop_index_statement
  //   | drop_principal_statement
  public static boolean drop_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_STATEMENT, "<drop statement>");
    result_ = drop_table_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_view_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_materialized_view_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_catalog_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_resource_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_repository_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_schema_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_index_statement(builder_, level_ + 1);
    if (!result_) result_ = drop_principal_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "DROP" "TABLE" if_exists? table_reference
  public static boolean drop_table_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_table_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_TABLE_STATEMENT, "<drop table statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "TABLE");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, drop_table_statement_2(builder_, level_ + 1));
    result_ = pinned_ && table_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean drop_table_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_table_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "DROP" "USER" security_principal
  public static boolean drop_user_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_user_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_USER_STATEMENT, "<drop user statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "USER");
    pinned_ = result_; // pin = 2
    result_ = result_ && security_principal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "DROP" "VIEW" if_exists? view_reference
  public static boolean drop_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DROP_VIEW_STATEMENT, "<drop view statement>");
    result_ = consumeToken(builder_, "DROP");
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, drop_view_statement_2(builder_, level_ + 1));
    result_ = pinned_ && view_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // if_exists?
  private static boolean drop_view_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "drop_view_statement_2")) return false;
    if_exists(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "EXISTS" "(" query_expression ")"
  public static boolean exists_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exists_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXISTS_EXPRESSION, "<exists expression>");
    result_ = consumeToken(builder_, "EXISTS");
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && query_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "EXPLAIN" ("ANALYZE" | "VERBOSE" | "LOGICAL" | "COSTS")* query_expression
  public static boolean explain_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "explain_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_EXPLAIN_STATEMENT, "<explain statement>");
    result_ = consumeToken(builder_, "EXPLAIN");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, explain_statement_1(builder_, level_ + 1));
    result_ = pinned_ && query_expression(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("ANALYZE" | "VERBOSE" | "LOGICAL" | "COSTS")*
  private static boolean explain_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "explain_statement_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!explain_statement_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "explain_statement_1", pos_)) break;
    }
    return true;
  }

  // "ANALYZE" | "VERBOSE" | "LOGICAL" | "COSTS"
  private static boolean explain_statement_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "explain_statement_1_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "ANALYZE");
    if (!result_) result_ = consumeToken(builder_, "VERBOSE");
    if (!result_) result_ = consumeToken(builder_, "LOGICAL");
    if (!result_) result_ = consumeToken(builder_, "COSTS");
    return result_;
  }

  /* ********************************************************** */
  // "EXPORT" "TABLE" table_reference "TO" string_literal properties_clause? | "CANCEL" "EXPORT" ("FROM" schema_reference)? where_clause?
  public static boolean export_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPORT_STATEMENT, "<export statement>");
    result_ = export_statement_0(builder_, level_ + 1);
    if (!result_) result_ = export_statement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::statement_recover);
    return result_;
  }

  // "EXPORT" "TABLE" table_reference "TO" string_literal properties_clause?
  private static boolean export_statement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "EXPORT");
    result_ = result_ && consumeToken(builder_, "TABLE");
    result_ = result_ && table_reference(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "TO");
    result_ = result_ && string_literal(builder_, level_ + 1);
    result_ = result_ && export_statement_0_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // properties_clause?
  private static boolean export_statement_0_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement_0_5")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  // "CANCEL" "EXPORT" ("FROM" schema_reference)? where_clause?
  private static boolean export_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "CANCEL");
    result_ = result_ && consumeToken(builder_, "EXPORT");
    result_ = result_ && export_statement_1_2(builder_, level_ + 1);
    result_ = result_ && export_statement_1_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("FROM" schema_reference)?
  private static boolean export_statement_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement_1_2")) return false;
    export_statement_1_2_0(builder_, level_ + 1);
    return true;
  }

  // "FROM" schema_reference
  private static boolean export_statement_1_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement_1_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "FROM");
    result_ = result_ && schema_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // where_clause?
  private static boolean export_statement_1_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "export_statement_1_3")) return false;
    where_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !("," | ")" | ";" | "FROM" | "WHERE" | "GROUP" | "HAVING" | "QUALIFY" | "WINDOW" | "ORDER" | "LIMIT" | "THEN" | "ELSE" | "END")
  static boolean expression_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !expression_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "," | ")" | ";" | "FROM" | "WHERE" | "GROUP" | "HAVING" | "QUALIFY" | "WINDOW" | "ORDER" | "LIMIT" | "THEN" | "ELSE" | "END"
  private static boolean expression_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ",");
    if (!result_) result_ = consumeToken(builder_, ")");
    if (!result_) result_ = consumeToken(builder_, ";");
    if (!result_) result_ = consumeToken(builder_, "FROM");
    if (!result_) result_ = consumeToken(builder_, "WHERE");
    if (!result_) result_ = consumeToken(builder_, "GROUP");
    if (!result_) result_ = consumeToken(builder_, "HAVING");
    if (!result_) result_ = consumeToken(builder_, "QUALIFY");
    if (!result_) result_ = consumeToken(builder_, "WINDOW");
    if (!result_) result_ = consumeToken(builder_, "ORDER");
    if (!result_) result_ = consumeToken(builder_, "LIMIT");
    if (!result_) result_ = consumeToken(builder_, "THEN");
    if (!result_) result_ = consumeToken(builder_, "ELSE");
    if (!result_) result_ = consumeToken(builder_, "END");
    return result_;
  }

  /* ********************************************************** */
  // "." identifier_reference
  public static boolean field_access_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field_access_tail")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FIELD_ACCESS_TAIL, "<field access tail>");
    result_ = consumeToken(builder_, ".");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "UNBOUNDED" ("PRECEDING" | "FOLLOWING") | "CURRENT" "ROW" | value_expression ("PRECEDING" | "FOLLOWING")
  public static boolean frame_bound(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_bound")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FRAME_BOUND, "<frame bound>");
    result_ = frame_bound_0(builder_, level_ + 1);
    if (!result_) result_ = frame_bound_1(builder_, level_ + 1);
    if (!result_) result_ = frame_bound_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "UNBOUNDED" ("PRECEDING" | "FOLLOWING")
  private static boolean frame_bound_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_bound_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "UNBOUNDED");
    result_ = result_ && frame_bound_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "PRECEDING" | "FOLLOWING"
  private static boolean frame_bound_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_bound_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "PRECEDING");
    if (!result_) result_ = consumeToken(builder_, "FOLLOWING");
    return result_;
  }

  // "CURRENT" "ROW"
  private static boolean frame_bound_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_bound_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "CURRENT");
    result_ = result_ && consumeToken(builder_, "ROW");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // value_expression ("PRECEDING" | "FOLLOWING")
  private static boolean frame_bound_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_bound_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = value_expression(builder_, level_ + 1);
    result_ = result_ && frame_bound_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "PRECEDING" | "FOLLOWING"
  private static boolean frame_bound_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_bound_2_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "PRECEDING");
    if (!result_) result_ = consumeToken(builder_, "FOLLOWING");
    return result_;
  }

  /* ********************************************************** */
  // ("ROWS" | "RANGE" | "GROUPS") frame_extent
  public static boolean frame_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FRAME_CLAUSE, "<frame clause>");
    result_ = frame_clause_0(builder_, level_ + 1);
    result_ = result_ && frame_extent(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "ROWS" | "RANGE" | "GROUPS"
  private static boolean frame_clause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_clause_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "ROWS");
    if (!result_) result_ = consumeToken(builder_, "RANGE");
    if (!result_) result_ = consumeToken(builder_, "GROUPS");
    return result_;
  }

  /* ********************************************************** */
  // "BETWEEN" frame_bound "AND" frame_bound | frame_bound
  public static boolean frame_extent(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_extent")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FRAME_EXTENT, "<frame extent>");
    result_ = frame_extent_0(builder_, level_ + 1);
    if (!result_) result_ = frame_bound(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "BETWEEN" frame_bound "AND" frame_bound
  private static boolean frame_extent_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "frame_extent_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "BETWEEN");
    result_ = result_ && frame_bound(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "AND");
    result_ = result_ && frame_bound(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "FROM" table_expression ("," table_expression)*
  public static boolean from_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_FROM_CLAUSE, "<from clause>");
    result_ = consumeToken(builder_, "FROM");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, table_expression(builder_, level_ + 1));
    result_ = pinned_ && from_clause_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::from_recover);
    return result_ || pinned_;
  }

  // ("," table_expression)*
  private static boolean from_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_clause_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!from_clause_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "from_clause_2", pos_)) break;
    }
    return true;
  }

  // "," table_expression
  private static boolean from_clause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_clause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && table_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "FROM" "KAFKA" property_list?
  public static boolean from_kafka_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_kafka_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FROM_KAFKA_CLAUSE, "<from kafka clause>");
    result_ = consumeToken(builder_, "FROM");
    result_ = result_ && consumeToken(builder_, "KAFKA");
    result_ = result_ && from_kafka_clause_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // property_list?
  private static boolean from_kafka_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_kafka_clause_2")) return false;
    property_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !(";" | "WHERE" | "GROUP" | "HAVING" | "QUALIFY" | "WINDOW" | "ORDER" | "LIMIT")
  static boolean from_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !from_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ";" | "WHERE" | "GROUP" | "HAVING" | "QUALIFY" | "WINDOW" | "ORDER" | "LIMIT"
  private static boolean from_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "from_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ";");
    if (!result_) result_ = consumeToken(builder_, "WHERE");
    if (!result_) result_ = consumeToken(builder_, "GROUP");
    if (!result_) result_ = consumeToken(builder_, "HAVING");
    if (!result_) result_ = consumeToken(builder_, "QUALIFY");
    if (!result_) result_ = consumeToken(builder_, "WINDOW");
    if (!result_) result_ = consumeToken(builder_, "ORDER");
    if (!result_) result_ = consumeToken(builder_, "LIMIT");
    return result_;
  }

  /* ********************************************************** */
  // ("DISTINCT" | "ALL")? value_expression ("," value_expression)* | "*"
  public static boolean function_argument_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_argument_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_ARGUMENT_LIST, "<function argument list>");
    result_ = function_argument_list_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "*");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("DISTINCT" | "ALL")? value_expression ("," value_expression)*
  private static boolean function_argument_list_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_argument_list_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = function_argument_list_0_0(builder_, level_ + 1);
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && function_argument_list_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("DISTINCT" | "ALL")?
  private static boolean function_argument_list_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_argument_list_0_0")) return false;
    function_argument_list_0_0_0(builder_, level_ + 1);
    return true;
  }

  // "DISTINCT" | "ALL"
  private static boolean function_argument_list_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_argument_list_0_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "DISTINCT");
    if (!result_) result_ = consumeToken(builder_, "ALL");
    return result_;
  }

  // ("," value_expression)*
  private static boolean function_argument_list_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_argument_list_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!function_argument_list_0_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "function_argument_list_0_2", pos_)) break;
    }
    return true;
  }

  // "," value_expression
  private static boolean function_argument_list_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_argument_list_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier "(" function_argument_list? ")"
  public static boolean function_call(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_call")) return false;
    if (!nextTokenIs(builder_, "<function call>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_FUNCTION_CALL, "<function call>");
    result_ = qualified_identifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && function_call_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // function_argument_list?
  private static boolean function_call_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_call_2")) return false;
    function_argument_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "GRANT" privilege_list "ON" privilege_target "TO" security_principal_list with_grant_option_clause?
  public static boolean grant_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grant_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GRANT_STATEMENT, "<grant statement>");
    result_ = consumeToken(builder_, "GRANT");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, privilege_list(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "ON")) && result_;
    result_ = pinned_ && report_error_(builder_, privilege_target(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "TO")) && result_;
    result_ = pinned_ && report_error_(builder_, security_principal_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && grant_statement_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // with_grant_option_clause?
  private static boolean grant_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grant_statement_6")) return false;
    with_grant_option_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "GROUP" "BY" grouping_item ("," grouping_item)*
  public static boolean group_by_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "group_by_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_GROUP_BY_CLAUSE, "<group by clause>");
    result_ = consumeToken(builder_, "GROUP");
    result_ = result_ && consumeToken(builder_, "BY");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, grouping_item(builder_, level_ + 1));
    result_ = pinned_ && group_by_clause_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // ("," grouping_item)*
  private static boolean group_by_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "group_by_clause_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!group_by_clause_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "group_by_clause_3", pos_)) break;
    }
    return true;
  }

  // "," grouping_item
  private static boolean group_by_clause_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "group_by_clause_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && grouping_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // value_expression | grouping_sets_clause
  public static boolean grouping_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grouping_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GROUPING_ITEM, "<grouping item>");
    result_ = value_expression(builder_, level_ + 1);
    if (!result_) result_ = grouping_sets_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ("GROUPING" "SETS" | "ROLLUP" | "CUBE") "(" grouping_item ("," grouping_item)* ")"
  public static boolean grouping_sets_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grouping_sets_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GROUPING_SETS_CLAUSE, "<grouping sets clause>");
    result_ = grouping_sets_clause_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && grouping_item(builder_, level_ + 1);
    result_ = result_ && grouping_sets_clause_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "GROUPING" "SETS" | "ROLLUP" | "CUBE"
  private static boolean grouping_sets_clause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grouping_sets_clause_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = grouping_sets_clause_0_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "ROLLUP");
    if (!result_) result_ = consumeToken(builder_, "CUBE");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "GROUPING" "SETS"
  private static boolean grouping_sets_clause_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grouping_sets_clause_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "GROUPING");
    result_ = result_ && consumeToken(builder_, "SETS");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("," grouping_item)*
  private static boolean grouping_sets_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grouping_sets_clause_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!grouping_sets_clause_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "grouping_sets_clause_3", pos_)) break;
    }
    return true;
  }

  // "," grouping_item
  private static boolean grouping_sets_clause_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "grouping_sets_clause_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && grouping_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "HAVING" value_expression
  public static boolean having_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "having_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_HAVING_CLAUSE, "<having clause>");
    result_ = consumeToken(builder_, "HAVING");
    pinned_ = result_; // pin = 1
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "IDENTIFIED" "BY" value_expression
  public static boolean identified_by_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identified_by_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IDENTIFIED_BY_CLAUSE, "<identified by clause>");
    result_ = consumeToken(builder_, "IDENTIFIED");
    result_ = result_ && consumeToken(builder_, "BY");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // SQL_IDENT
  public static boolean identifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifier")) return false;
    if (!nextTokenIs(builder_, SQL_IDENT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SQL_IDENT);
    exit_section_(builder_, marker_, IDENTIFIER, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier | delimited_identifier
  public static boolean identifier_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifier_reference")) return false;
    if (!nextTokenIs(builder_, "<identifier reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IDENTIFIER_REFERENCE, "<identifier reference>");
    result_ = identifier(builder_, level_ + 1);
    if (!result_) result_ = delimited_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "IF" "EXISTS"
  static boolean if_exists(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_exists")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "IF");
    result_ = result_ && consumeToken(builder_, "EXISTS");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "IF" "NOT" "EXISTS"
  static boolean if_not_exists(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_not_exists")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "IF");
    result_ = result_ && consumeToken(builder_, "NOT");
    result_ = result_ && consumeToken(builder_, "EXISTS");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "(" (query_expression | value_expression ("," value_expression)*)? ")"
  public static boolean in_list_or_subquery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "in_list_or_subquery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IN_LIST_OR_SUBQUERY, "<in list or subquery>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && in_list_or_subquery_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (query_expression | value_expression ("," value_expression)*)?
  private static boolean in_list_or_subquery_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "in_list_or_subquery_1")) return false;
    in_list_or_subquery_1_0(builder_, level_ + 1);
    return true;
  }

  // query_expression | value_expression ("," value_expression)*
  private static boolean in_list_or_subquery_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "in_list_or_subquery_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = query_expression(builder_, level_ + 1);
    if (!result_) result_ = in_list_or_subquery_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // value_expression ("," value_expression)*
  private static boolean in_list_or_subquery_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "in_list_or_subquery_1_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = value_expression(builder_, level_ + 1);
    result_ = result_ && in_list_or_subquery_1_0_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ("," value_expression)*
  private static boolean in_list_or_subquery_1_0_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "in_list_or_subquery_1_0_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!in_list_or_subquery_1_0_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "in_list_or_subquery_1_0_1_1", pos_)) break;
    }
    return true;
  }

  // "," value_expression
  private static boolean in_list_or_subquery_1_0_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "in_list_or_subquery_1_0_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean index_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_reference")) return false;
    if (!nextTokenIs(builder_, "<index reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INDEX_REFERENCE, "<index reference>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "INSERT" ("INTO" | "OVERWRITE") "TABLE"? dml_target_table table_column_list? (query_expression | values_clause)
  public static boolean insert_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "insert_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_INSERT_STATEMENT, "<insert statement>");
    result_ = consumeToken(builder_, "INSERT");
    result_ = result_ && insert_statement_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, insert_statement_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, dml_target_table(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, insert_statement_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && insert_statement_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "INTO" | "OVERWRITE"
  private static boolean insert_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "insert_statement_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "INTO");
    if (!result_) result_ = consumeToken(builder_, "OVERWRITE");
    return result_;
  }

  // "TABLE"?
  private static boolean insert_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "insert_statement_2")) return false;
    consumeToken(builder_, "TABLE");
    return true;
  }

  // table_column_list?
  private static boolean insert_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "insert_statement_4")) return false;
    table_column_list(builder_, level_ + 1);
    return true;
  }

  // query_expression | values_clause
  private static boolean insert_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "insert_statement_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = query_expression(builder_, level_ + 1);
    if (!result_) result_ = values_clause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "INTERVAL" value_expression interval_unit
  public static boolean interval_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "interval_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INTERVAL_EXPRESSION, "<interval expression>");
    result_ = consumeToken(builder_, "INTERVAL");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && interval_unit(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "MICROSECOND" | "MILLISECOND" | "SECOND" | "SECONDS" | "MINUTE" | "MINUTES" | "HOUR" | "HOURS" | "DAY" | "DAYS" | "WEEK" | "MONTH" | "QUARTER" | "YEAR"
  public static boolean interval_unit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "interval_unit")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INTERVAL_UNIT, "<interval unit>");
    result_ = consumeToken(builder_, "MICROSECOND");
    if (!result_) result_ = consumeToken(builder_, "MILLISECOND");
    if (!result_) result_ = consumeToken(builder_, "SECOND");
    if (!result_) result_ = consumeToken(builder_, "SECONDS");
    if (!result_) result_ = consumeToken(builder_, "MINUTE");
    if (!result_) result_ = consumeToken(builder_, "MINUTES");
    if (!result_) result_ = consumeToken(builder_, "HOUR");
    if (!result_) result_ = consumeToken(builder_, "HOURS");
    if (!result_) result_ = consumeToken(builder_, "DAY");
    if (!result_) result_ = consumeToken(builder_, "DAYS");
    if (!result_) result_ = consumeToken(builder_, "WEEK");
    if (!result_) result_ = consumeToken(builder_, "MONTH");
    if (!result_) result_ = consumeToken(builder_, "QUARTER");
    if (!result_) result_ = consumeToken(builder_, "YEAR");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "ON" value_expression | using_clause
  public static boolean join_condition_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_condition_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_JOIN_CONDITION_CLAUSE, "<join condition clause>");
    result_ = join_condition_clause_0(builder_, level_ + 1);
    if (!result_) result_ = using_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "ON" value_expression
  private static boolean join_condition_clause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_condition_clause_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "ON");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // join_operator table_expression join_condition_clause?
  public static boolean join_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_expression")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_JOIN_EXPRESSION, "<join expression>");
    result_ = join_operator(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, table_expression(builder_, level_ + 1));
    result_ = pinned_ && join_expression_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::from_recover);
    return result_ || pinned_;
  }

  // join_condition_clause?
  private static boolean join_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_expression_2")) return false;
    join_condition_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ("INNER" | "LEFT" "OUTER"? | "RIGHT" "OUTER"? | "FULL" "OUTER"? | "CROSS" | "NATURAL" | "SEMI" | "ANTI")? ("JOIN" | "STRAIGHT_JOIN")
  public static boolean join_operator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, JOIN_OPERATOR, "<join operator>");
    result_ = join_operator_0(builder_, level_ + 1);
    result_ = result_ && join_operator_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("INNER" | "LEFT" "OUTER"? | "RIGHT" "OUTER"? | "FULL" "OUTER"? | "CROSS" | "NATURAL" | "SEMI" | "ANTI")?
  private static boolean join_operator_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0")) return false;
    join_operator_0_0(builder_, level_ + 1);
    return true;
  }

  // "INNER" | "LEFT" "OUTER"? | "RIGHT" "OUTER"? | "FULL" "OUTER"? | "CROSS" | "NATURAL" | "SEMI" | "ANTI"
  private static boolean join_operator_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "INNER");
    if (!result_) result_ = join_operator_0_0_1(builder_, level_ + 1);
    if (!result_) result_ = join_operator_0_0_2(builder_, level_ + 1);
    if (!result_) result_ = join_operator_0_0_3(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "CROSS");
    if (!result_) result_ = consumeToken(builder_, "NATURAL");
    if (!result_) result_ = consumeToken(builder_, "SEMI");
    if (!result_) result_ = consumeToken(builder_, "ANTI");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "LEFT" "OUTER"?
  private static boolean join_operator_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "LEFT");
    result_ = result_ && join_operator_0_0_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "OUTER"?
  private static boolean join_operator_0_0_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0_1_1")) return false;
    consumeToken(builder_, "OUTER");
    return true;
  }

  // "RIGHT" "OUTER"?
  private static boolean join_operator_0_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "RIGHT");
    result_ = result_ && join_operator_0_0_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "OUTER"?
  private static boolean join_operator_0_0_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0_2_1")) return false;
    consumeToken(builder_, "OUTER");
    return true;
  }

  // "FULL" "OUTER"?
  private static boolean join_operator_0_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "FULL");
    result_ = result_ && join_operator_0_0_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "OUTER"?
  private static boolean join_operator_0_0_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_0_0_3_1")) return false;
    consumeToken(builder_, "OUTER");
    return true;
  }

  // "JOIN" | "STRAIGHT_JOIN"
  private static boolean join_operator_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "join_operator_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "JOIN");
    if (!result_) result_ = consumeToken(builder_, "STRAIGHT_JOIN");
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean key_column(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_column")) return false;
    if (!nextTokenIs(builder_, "<key column>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_COLUMN, "<key column>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "(" key_column ("," key_column)* ","? ")"
  public static boolean key_column_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_column_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_COLUMN_LIST, "<key column list>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && key_column(builder_, level_ + 1);
    result_ = result_ && key_column_list_2(builder_, level_ + 1);
    result_ = result_ && key_column_list_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," key_column)*
  private static boolean key_column_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_column_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!key_column_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "key_column_list_2", pos_)) break;
    }
    return true;
  }

  // "," key_column
  private static boolean key_column_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_column_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && key_column(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ","?
  private static boolean key_column_list_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_column_list_3")) return false;
    consumeToken(builder_, ",");
    return true;
  }

  /* ********************************************************** */
  // "PRIMARY" | "DUPLICATE" | "UNIQUE" | "AGGREGATE"
  static boolean key_model(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_model")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "PRIMARY");
    if (!result_) result_ = consumeToken(builder_, "DUPLICATE");
    if (!result_) result_ = consumeToken(builder_, "UNIQUE");
    if (!result_) result_ = consumeToken(builder_, "AGGREGATE");
    return result_;
  }

  /* ********************************************************** */
  // key_model "KEY" key_column_list
  public static boolean key_model_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_model_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_MODEL_CLAUSE, "<key model clause>");
    result_ = key_model(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "KEY");
    pinned_ = result_; // pin = 2
    result_ = result_ && key_column_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "TRUE" | "FALSE" | "NULL" | "CURRENT_DATE" | "CURRENT_TIME" | "CURRENT_TIMESTAMP" | "LOCALTIME" | "LOCALTIMESTAMP"
  public static boolean keyword_literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "keyword_literal")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEYWORD_LITERAL, "<keyword literal>");
    result_ = consumeToken(builder_, "TRUE");
    if (!result_) result_ = consumeToken(builder_, "FALSE");
    if (!result_) result_ = consumeToken(builder_, "NULL");
    if (!result_) result_ = consumeToken(builder_, "CURRENT_DATE");
    if (!result_) result_ = consumeToken(builder_, "CURRENT_TIME");
    if (!result_) result_ = consumeToken(builder_, "CURRENT_TIMESTAMP");
    if (!result_) result_ = consumeToken(builder_, "LOCALTIME");
    if (!result_) result_ = consumeToken(builder_, "LOCALTIMESTAMP");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "KILL" value_expression
  public static boolean kill_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "kill_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KILL_STATEMENT, "<kill statement>");
    result_ = consumeToken(builder_, "KILL");
    pinned_ = result_; // pin = 1
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "LIMIT" limit_expression ("," limit_expression)?
  public static boolean limit_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "limit_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_LIMIT_CLAUSE, "<limit clause>");
    result_ = consumeToken(builder_, "LIMIT");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, limit_expression(builder_, level_ + 1));
    result_ = pinned_ && limit_clause_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // ("," limit_expression)?
  private static boolean limit_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "limit_clause_2")) return false;
    limit_clause_2_0(builder_, level_ + 1);
    return true;
  }

  // "," limit_expression
  private static boolean limit_clause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "limit_clause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && limit_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // value_expression
  public static boolean limit_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "limit_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LIMIT_EXPRESSION, "<limit expression>");
    result_ = value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // string_literal | numeric_literal | parameter_literal | keyword_literal
  public static boolean literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "literal")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LITERAL, "<literal>");
    result_ = string_literal(builder_, level_ + 1);
    if (!result_) result_ = numeric_literal(builder_, level_ + 1);
    if (!result_) result_ = parameter_literal(builder_, level_ + 1);
    if (!result_) result_ = keyword_literal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference+
  static boolean load_payload(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "load_payload")) return false;
    if (!nextTokenIs(builder_, "", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier_reference(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!identifier_reference(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "load_payload", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "LOAD" "LABEL" identifier_reference "(" load_payload ")" ("WITH" "BROKER")? properties_clause?
  public static boolean load_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "load_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOAD_STATEMENT, "<load statement>");
    result_ = consumeToken(builder_, "LOAD");
    result_ = result_ && consumeToken(builder_, "LABEL");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "(")) && result_;
    result_ = pinned_ && report_error_(builder_, load_payload(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, ")")) && result_;
    result_ = pinned_ && report_error_(builder_, load_statement_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && load_statement_7(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("WITH" "BROKER")?
  private static boolean load_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "load_statement_6")) return false;
    load_statement_6_0(builder_, level_ + 1);
    return true;
  }

  // "WITH" "BROKER"
  private static boolean load_statement_6_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "load_statement_6_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "WITH");
    result_ = result_ && consumeToken(builder_, "BROKER");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // properties_clause?
  private static boolean load_statement_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "load_statement_7")) return false;
    properties_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "MAP" "<" type_element "," type_element ">"
  public static boolean map_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "map_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MAP_TYPE, "<map type>");
    result_ = consumeToken(builder_, "MAP");
    result_ = result_ && consumeToken(builder_, "<");
    result_ = result_ && type_element(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ",");
    result_ = result_ && type_element(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ">");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean materialized_view_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "materialized_view_reference")) return false;
    if (!nextTokenIs(builder_, "<materialized view reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MATERIALIZED_VIEW_REFERENCE, "<materialized view reference>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "ACTIVE" | "INACTIVE" | "SUSPEND" | "RESUME"
  static boolean materialized_view_status(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "materialized_view_status")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "ACTIVE");
    if (!result_) result_ = consumeToken(builder_, "INACTIVE");
    if (!result_) result_ = consumeToken(builder_, "SUSPEND");
    if (!result_) result_ = consumeToken(builder_, "RESUME");
    return result_;
  }

  /* ********************************************************** */
  // "ON" value_expression
  public static boolean merge_on_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_on_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MERGE_ON_CLAUSE, "<merge on clause>");
    result_ = consumeToken(builder_, "ON");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "MERGE" "INTO" dml_target_table merge_using_clause merge_on_clause merge_when_clause+
  public static boolean merge_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_MERGE_STATEMENT, "<merge statement>");
    result_ = consumeToken(builder_, "MERGE");
    result_ = result_ && consumeToken(builder_, "INTO");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, dml_target_table(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, merge_using_clause(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, merge_on_clause(builder_, level_ + 1)) && result_;
    result_ = pinned_ && merge_statement_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // merge_when_clause+
  private static boolean merge_statement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_statement_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = merge_when_clause(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!merge_when_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "merge_statement_5", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "USING" table_expression
  public static boolean merge_using_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_using_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MERGE_USING_CLAUSE, "<merge using clause>");
    result_ = consumeToken(builder_, "USING");
    result_ = result_ && table_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "WHEN" ("MATCHED" | "NOT" "MATCHED") ("THEN" ("UPDATE" set_clause | "DELETE" | "INSERT" values_clause))
  public static boolean merge_when_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MERGE_WHEN_CLAUSE, "<merge when clause>");
    result_ = consumeToken(builder_, "WHEN");
    result_ = result_ && merge_when_clause_1(builder_, level_ + 1);
    result_ = result_ && merge_when_clause_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "MATCHED" | "NOT" "MATCHED"
  private static boolean merge_when_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "MATCHED");
    if (!result_) result_ = merge_when_clause_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "NOT" "MATCHED"
  private static boolean merge_when_clause_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "NOT");
    result_ = result_ && consumeToken(builder_, "MATCHED");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "THEN" ("UPDATE" set_clause | "DELETE" | "INSERT" values_clause)
  private static boolean merge_when_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "THEN");
    result_ = result_ && merge_when_clause_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "UPDATE" set_clause | "DELETE" | "INSERT" values_clause
  private static boolean merge_when_clause_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause_2_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = merge_when_clause_2_1_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "DELETE");
    if (!result_) result_ = merge_when_clause_2_1_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "UPDATE" set_clause
  private static boolean merge_when_clause_2_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause_2_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "UPDATE");
    result_ = result_ && set_clause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "INSERT" values_clause
  private static boolean merge_when_clause_2_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "merge_when_clause_2_1_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "INSERT");
    result_ = result_ && values_clause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ("MODIFY" | "CHANGE" | "ALTER") "COLUMN"? column_definition
  public static boolean modify_column_action(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "modify_column_action")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MODIFY_COLUMN_ACTION, "<modify column action>");
    result_ = modify_column_action_0(builder_, level_ + 1);
    result_ = result_ && modify_column_action_1(builder_, level_ + 1);
    result_ = result_ && column_definition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "MODIFY" | "CHANGE" | "ALTER"
  private static boolean modify_column_action_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "modify_column_action_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "MODIFY");
    if (!result_) result_ = consumeToken(builder_, "CHANGE");
    if (!result_) result_ = consumeToken(builder_, "ALTER");
    return result_;
  }

  // "COLUMN"?
  private static boolean modify_column_action_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "modify_column_action_1")) return false;
    consumeToken(builder_, "COLUMN");
    return true;
  }

  /* ********************************************************** */
  // unary_expression (("*" | "/" | "%" | "DIV" | "MOD") unary_expression)*
  public static boolean multiplicative_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicative_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MULTIPLICATIVE_EXPRESSION, "<multiplicative expression>");
    result_ = unary_expression(builder_, level_ + 1);
    result_ = result_ && multiplicative_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (("*" | "/" | "%" | "DIV" | "MOD") unary_expression)*
  private static boolean multiplicative_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicative_expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!multiplicative_expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "multiplicative_expression_1", pos_)) break;
    }
    return true;
  }

  // ("*" | "/" | "%" | "DIV" | "MOD") unary_expression
  private static boolean multiplicative_expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicative_expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = multiplicative_expression_1_0_0(builder_, level_ + 1);
    result_ = result_ && unary_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "*" | "/" | "%" | "DIV" | "MOD"
  private static boolean multiplicative_expression_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicative_expression_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "*");
    if (!result_) result_ = consumeToken(builder_, "/");
    if (!result_) result_ = consumeToken(builder_, "%");
    if (!result_) result_ = consumeToken(builder_, "DIV");
    if (!result_) result_ = consumeToken(builder_, "MOD");
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference cte_column_list? "AS" parenthesized_query_expression
  public static boolean named_query_definition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "named_query_definition")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_NAMED_QUERY_DEFINITION, "<named query definition>");
    result_ = identifier_reference(builder_, level_ + 1);
    result_ = result_ && named_query_definition_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "AS");
    pinned_ = result_; // pin = 3
    result_ = result_ && parenthesized_query_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // cte_column_list?
  private static boolean named_query_definition_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "named_query_definition_1")) return false;
    cte_column_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "NOT"? comparison_expression
  public static boolean not_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "not_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NOT_EXPRESSION, "<not expression>");
    result_ = not_expression_0(builder_, level_ + 1);
    result_ = result_ && comparison_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "NOT"?
  private static boolean not_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "not_expression_0")) return false;
    consumeToken(builder_, "NOT");
    return true;
  }

  /* ********************************************************** */
  // SQL_INTEGER_TOKEN | SQL_FLOAT_TOKEN
  public static boolean numeric_literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "numeric_literal")) return false;
    if (!nextTokenIs(builder_, "<numeric literal>", SQL_FLOAT_TOKEN, SQL_INTEGER_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NUMERIC_LITERAL, "<numeric literal>");
    result_ = consumeToken(builder_, SQL_INTEGER_TOKEN);
    if (!result_) result_ = consumeToken(builder_, SQL_FLOAT_TOKEN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // and_expression ("OR" and_expression)*
  public static boolean or_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OR_EXPRESSION, "<or expression>");
    result_ = and_expression(builder_, level_ + 1);
    result_ = result_ && or_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("OR" and_expression)*
  private static boolean or_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!or_expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "or_expression_1", pos_)) break;
    }
    return true;
  }

  // "OR" and_expression
  private static boolean or_expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "OR");
    result_ = result_ && and_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "ORDER" "BY" ordering_item ("," ordering_item)*
  public static boolean order_by_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "order_by_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_ORDER_BY_CLAUSE, "<order by clause>");
    result_ = consumeToken(builder_, "ORDER");
    result_ = result_ && consumeToken(builder_, "BY");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, ordering_item(builder_, level_ + 1));
    result_ = pinned_ && order_by_clause_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // ("," ordering_item)*
  private static boolean order_by_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "order_by_clause_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!order_by_clause_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "order_by_clause_3", pos_)) break;
    }
    return true;
  }

  // "," ordering_item
  private static boolean order_by_clause_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "order_by_clause_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && ordering_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // value_expression ("ASC" | "DESC")? ("NULLS" ("FIRST" | "LAST"))?
  public static boolean ordering_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ordering_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ORDERING_ITEM, "<ordering item>");
    result_ = value_expression(builder_, level_ + 1);
    result_ = result_ && ordering_item_1(builder_, level_ + 1);
    result_ = result_ && ordering_item_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("ASC" | "DESC")?
  private static boolean ordering_item_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ordering_item_1")) return false;
    ordering_item_1_0(builder_, level_ + 1);
    return true;
  }

  // "ASC" | "DESC"
  private static boolean ordering_item_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ordering_item_1_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "ASC");
    if (!result_) result_ = consumeToken(builder_, "DESC");
    return result_;
  }

  // ("NULLS" ("FIRST" | "LAST"))?
  private static boolean ordering_item_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ordering_item_2")) return false;
    ordering_item_2_0(builder_, level_ + 1);
    return true;
  }

  // "NULLS" ("FIRST" | "LAST")
  private static boolean ordering_item_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ordering_item_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "NULLS");
    result_ = result_ && ordering_item_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "FIRST" | "LAST"
  private static boolean ordering_item_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ordering_item_2_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "FIRST");
    if (!result_) result_ = consumeToken(builder_, "LAST");
    return result_;
  }

  /* ********************************************************** */
  // show_statement
  //   | admin_statement
  //   | analyze_statement
  //   | set_statement
  //   | set_password_statement
  //   | unset_statement
  //   | kill_statement
  //   | sync_statement
  //   | call_statement
  //   | transaction_statement
  //   | use_statement
  //   | explain_statement
  //   | describe_statement
  //   | load_statement
  //   | cancel_load_statement
  //   | task_statement
  //   | export_statement
  //   | backup_restore_statement
  public static boolean other_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "other_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OTHER_STATEMENT, "<other statement>");
    result_ = show_statement(builder_, level_ + 1);
    if (!result_) result_ = admin_statement(builder_, level_ + 1);
    if (!result_) result_ = analyze_statement(builder_, level_ + 1);
    if (!result_) result_ = set_statement(builder_, level_ + 1);
    if (!result_) result_ = set_password_statement(builder_, level_ + 1);
    if (!result_) result_ = unset_statement(builder_, level_ + 1);
    if (!result_) result_ = kill_statement(builder_, level_ + 1);
    if (!result_) result_ = sync_statement(builder_, level_ + 1);
    if (!result_) result_ = call_statement(builder_, level_ + 1);
    if (!result_) result_ = transaction_statement(builder_, level_ + 1);
    if (!result_) result_ = use_statement(builder_, level_ + 1);
    if (!result_) result_ = explain_statement(builder_, level_ + 1);
    if (!result_) result_ = describe_statement(builder_, level_ + 1);
    if (!result_) result_ = load_statement(builder_, level_ + 1);
    if (!result_) result_ = cancel_load_statement(builder_, level_ + 1);
    if (!result_) result_ = task_statement(builder_, level_ + 1);
    if (!result_) result_ = export_statement(builder_, level_ + 1);
    if (!result_) result_ = backup_restore_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "SHOW" | "ADMIN" | "ANALYZE" | "SET" | "UNSET" | "KILL" | "SYNC" | "CALL" | "BEGIN" | "START" | "COMMIT" | "ROLLBACK" | "USE" | "EXPLAIN" | "DESC" | "DESCRIBE" | "LOAD" | "CANCEL" | "SUBMIT" | "EXPORT" | "BACKUP" | "RESTORE" | "RECOVER"
  static boolean other_statement_start(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "other_statement_start")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "SHOW");
    if (!result_) result_ = consumeToken(builder_, "ADMIN");
    if (!result_) result_ = consumeToken(builder_, "ANALYZE");
    if (!result_) result_ = consumeToken(builder_, "SET");
    if (!result_) result_ = consumeToken(builder_, "UNSET");
    if (!result_) result_ = consumeToken(builder_, "KILL");
    if (!result_) result_ = consumeToken(builder_, "SYNC");
    if (!result_) result_ = consumeToken(builder_, "CALL");
    if (!result_) result_ = consumeToken(builder_, "BEGIN");
    if (!result_) result_ = consumeToken(builder_, "START");
    if (!result_) result_ = consumeToken(builder_, "COMMIT");
    if (!result_) result_ = consumeToken(builder_, "ROLLBACK");
    if (!result_) result_ = consumeToken(builder_, "USE");
    if (!result_) result_ = consumeToken(builder_, "EXPLAIN");
    if (!result_) result_ = consumeToken(builder_, "DESC");
    if (!result_) result_ = consumeToken(builder_, "DESCRIBE");
    if (!result_) result_ = consumeToken(builder_, "LOAD");
    if (!result_) result_ = consumeToken(builder_, "CANCEL");
    if (!result_) result_ = consumeToken(builder_, "SUBMIT");
    if (!result_) result_ = consumeToken(builder_, "EXPORT");
    if (!result_) result_ = consumeToken(builder_, "BACKUP");
    if (!result_) result_ = consumeToken(builder_, "RESTORE");
    if (!result_) result_ = consumeToken(builder_, "RECOVER");
    return result_;
  }

  /* ********************************************************** */
  // STARROCKS_PARAMETER
  public static boolean parameter_literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_literal")) return false;
    if (!nextTokenIs(builder_, STARROCKS_PARAMETER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STARROCKS_PARAMETER);
    exit_section_(builder_, marker_, PARAMETER_LITERAL, result_);
    return result_;
  }

  /* ********************************************************** */
  // "(" identifier_reference ("," identifier_reference)* ","? ")"
  public static boolean parenthesized_identifier_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_identifier_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARENTHESIZED_IDENTIFIER_LIST, "<parenthesized identifier list>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    result_ = result_ && parenthesized_identifier_list_2(builder_, level_ + 1);
    result_ = result_ && parenthesized_identifier_list_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," identifier_reference)*
  private static boolean parenthesized_identifier_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_identifier_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parenthesized_identifier_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parenthesized_identifier_list_2", pos_)) break;
    }
    return true;
  }

  // "," identifier_reference
  private static boolean parenthesized_identifier_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_identifier_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ","?
  private static boolean parenthesized_identifier_list_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_identifier_list_3")) return false;
    consumeToken(builder_, ",");
    return true;
  }

  /* ********************************************************** */
  // "(" table_expression ")"
  public static boolean parenthesized_join_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_join_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_PARENTHESIZED_JOIN_EXPRESSION, "<parenthesized join expression>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && table_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "(" query_expression ")"
  public static boolean parenthesized_query_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_query_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_PARENTHESIZED_QUERY_EXPRESSION, "<parenthesized query expression>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && query_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "(" value_expression ("," value_expression)* ")"
  public static boolean parenthesized_value_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_value_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARENTHESIZED_VALUE_EXPRESSION, "<parenthesized value expression>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && parenthesized_value_expression_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," value_expression)*
  private static boolean parenthesized_value_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_value_expression_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parenthesized_value_expression_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parenthesized_value_expression_2", pos_)) break;
    }
    return true;
  }

  // "," value_expression
  private static boolean parenthesized_value_expression_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesized_value_expression_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "PARTITION" "BY" value_expression ("," value_expression)*
  public static boolean partition_by_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partition_by_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARTITION_BY_CLAUSE, "<partition by clause>");
    result_ = consumeToken(builder_, "PARTITION");
    result_ = result_ && consumeToken(builder_, "BY");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && partition_by_clause_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," value_expression)*
  private static boolean partition_by_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partition_by_clause_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!partition_by_clause_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "partition_by_clause_3", pos_)) break;
    }
    return true;
  }

  // "," value_expression
  private static boolean partition_by_clause_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partition_by_clause_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "PARTITION" "BY" partition_expression
  public static boolean partition_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partition_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARTITION_CLAUSE, "<partition clause>");
    result_ = consumeToken(builder_, "PARTITION");
    result_ = result_ && consumeToken(builder_, "BY");
    pinned_ = result_; // pin = 2
    result_ = result_ && partition_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // function_call | parenthesized_value_expression | identifier_reference
  public static boolean partition_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partition_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARTITION_EXPRESSION, "<partition expression>");
    result_ = function_call(builder_, level_ + 1);
    if (!result_) result_ = parenthesized_value_expression(builder_, level_ + 1);
    if (!result_) result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // primary_expression analytic_clause? array_access_tail* field_access_tail*
  public static boolean postfix_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, POSTFIX_EXPRESSION, "<postfix expression>");
    result_ = primary_expression(builder_, level_ + 1);
    result_ = result_ && postfix_expression_1(builder_, level_ + 1);
    result_ = result_ && postfix_expression_2(builder_, level_ + 1);
    result_ = result_ && postfix_expression_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // analytic_clause?
  private static boolean postfix_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expression_1")) return false;
    analytic_clause(builder_, level_ + 1);
    return true;
  }

  // array_access_tail*
  private static boolean postfix_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expression_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!array_access_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "postfix_expression_2", pos_)) break;
    }
    return true;
  }

  // field_access_tail*
  private static boolean postfix_expression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expression_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!field_access_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "postfix_expression_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // cast_expression
  //   | case_expression
  //   | exists_expression
  //   | interval_expression
  //   | typed_literal_expression
  //   | function_call
  //   | parenthesized_value_expression
  //   | column_reference
  //   | literal
  public static boolean primary_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primary_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_EXPRESSION, "<primary expression>");
    result_ = cast_expression(builder_, level_ + 1);
    if (!result_) result_ = case_expression(builder_, level_ + 1);
    if (!result_) result_ = exists_expression(builder_, level_ + 1);
    if (!result_) result_ = interval_expression(builder_, level_ + 1);
    if (!result_) result_ = typed_literal_expression(builder_, level_ + 1);
    if (!result_) result_ = function_call(builder_, level_ + 1);
    if (!result_) result_ = parenthesized_value_expression(builder_, level_ + 1);
    if (!result_) result_ = column_reference(builder_, level_ + 1);
    if (!result_) result_ = literal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identified_by_clause | default_role_clause | properties_clause
  public static boolean principal_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "principal_tail")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRINCIPAL_TAIL, "<principal tail>");
    result_ = identified_by_clause(builder_, level_ + 1);
    if (!result_) result_ = default_role_clause(builder_, level_ + 1);
    if (!result_) result_ = properties_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "USER" | "ROLE"
  static boolean principal_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "principal_type")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "USER");
    if (!result_) result_ = consumeToken(builder_, "ROLE");
    return result_;
  }

  /* ********************************************************** */
  // "*" | identifier_reference
  public static boolean privilege_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIVILEGE_ITEM, "<privilege item>");
    result_ = consumeToken(builder_, "*");
    if (!result_) result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // privilege_item ("," privilege_item)*
  public static boolean privilege_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIVILEGE_LIST, "<privilege list>");
    result_ = privilege_item(builder_, level_ + 1);
    result_ = result_ && privilege_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," privilege_item)*
  private static boolean privilege_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!privilege_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "privilege_list_1", pos_)) break;
    }
    return true;
  }

  // "," privilege_item
  private static boolean privilege_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && privilege_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ("TABLE" | "DATABASE" | "CATALOG" | "RESOURCE" | "SYSTEM")? identifier_reference
  public static boolean privilege_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIVILEGE_TARGET, "<privilege target>");
    result_ = privilege_target_0(builder_, level_ + 1);
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("TABLE" | "DATABASE" | "CATALOG" | "RESOURCE" | "SYSTEM")?
  private static boolean privilege_target_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_target_0")) return false;
    privilege_target_0_0(builder_, level_ + 1);
    return true;
  }

  // "TABLE" | "DATABASE" | "CATALOG" | "RESOURCE" | "SYSTEM"
  private static boolean privilege_target_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "privilege_target_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "TABLE");
    if (!result_) result_ = consumeToken(builder_, "DATABASE");
    if (!result_) result_ = consumeToken(builder_, "CATALOG");
    if (!result_) result_ = consumeToken(builder_, "RESOURCE");
    if (!result_) result_ = consumeToken(builder_, "SYSTEM");
    return result_;
  }

  /* ********************************************************** */
  // "PROPERTIES" property_list
  public static boolean properties_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "properties_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTIES_CLAUSE, "<properties clause>");
    result_ = consumeToken(builder_, "PROPERTIES");
    pinned_ = result_; // pin = 1
    result_ = result_ && property_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // string_literal | identifier_reference
  public static boolean property_key(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_key")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY_KEY, "<property key>");
    result_ = string_literal(builder_, level_ + 1);
    if (!result_) result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "(" property_pair ("," property_pair)* ","? ")"
  public static boolean property_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY_LIST, "<property list>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && property_pair(builder_, level_ + 1);
    result_ = result_ && property_list_2(builder_, level_ + 1);
    result_ = result_ && property_list_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," property_pair)*
  private static boolean property_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!property_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "property_list_2", pos_)) break;
    }
    return true;
  }

  // "," property_pair
  private static boolean property_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && property_pair(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ","?
  private static boolean property_list_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_list_3")) return false;
    consumeToken(builder_, ",");
    return true;
  }

  /* ********************************************************** */
  // property_key "=" property_value
  public static boolean property_pair(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_pair")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY_PAIR, "<property pair>");
    result_ = property_key(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "=");
    result_ = result_ && property_value(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // string_literal | numeric_literal | keyword_literal | identifier_reference
  public static boolean property_value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_value")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY_VALUE, "<property value>");
    result_ = string_literal(builder_, level_ + 1);
    if (!result_) result_ = numeric_literal(builder_, level_ + 1);
    if (!result_) result_ = keyword_literal(builder_, level_ + 1);
    if (!result_) result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference ("." identifier_reference)* "."
  public static boolean qualified_column_prefix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_column_prefix")) return false;
    if (!nextTokenIs(builder_, "<qualified column prefix>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUALIFIED_COLUMN_PREFIX, "<qualified column prefix>");
    result_ = identifier_reference(builder_, level_ + 1);
    result_ = result_ && qualified_column_prefix_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ".");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("." identifier_reference)*
  private static boolean qualified_column_prefix_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_column_prefix_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!qualified_column_prefix_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "qualified_column_prefix_1", pos_)) break;
    }
    return true;
  }

  // "." identifier_reference
  private static boolean qualified_column_prefix_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_column_prefix_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ".");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference ("." identifier_reference)*
  public static boolean qualified_identifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_identifier")) return false;
    if (!nextTokenIs(builder_, "<qualified identifier>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUALIFIED_IDENTIFIER, "<qualified identifier>");
    result_ = identifier_reference(builder_, level_ + 1);
    result_ = result_ && qualified_identifier_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("." identifier_reference)*
  private static boolean qualified_identifier_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_identifier_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!qualified_identifier_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "qualified_identifier_1", pos_)) break;
    }
    return true;
  }

  // "." identifier_reference
  private static boolean qualified_identifier_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_identifier_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ".");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "QUALIFY" qualify_expression
  public static boolean qualify_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualify_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_QUALIFY_CLAUSE, "<qualify clause>");
    result_ = consumeToken(builder_, "QUALIFY");
    pinned_ = result_; // pin = 1
    result_ = result_ && qualify_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // value_expression
  public static boolean qualify_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualify_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUALIFY_EXPRESSION, "<qualify expression>");
    result_ = value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // with_query_expression | set_query_expression | simple_query_expression | values_expression
  public static boolean query_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "query_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_QUERY_EXPRESSION, "<query expression>");
    result_ = with_query_expression(builder_, level_ + 1);
    if (!result_) result_ = set_query_expression(builder_, level_ + 1);
    if (!result_) result_ = simple_query_expression(builder_, level_ + 1);
    if (!result_) result_ = values_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::query_recover);
    return result_;
  }

  /* ********************************************************** */
  // !(";" | ")" | "UNION" | "INTERSECT" | "EXCEPT" | "MINUS")
  static boolean query_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "query_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !query_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ";" | ")" | "UNION" | "INTERSECT" | "EXCEPT" | "MINUS"
  private static boolean query_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "query_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ";");
    if (!result_) result_ = consumeToken(builder_, ")");
    if (!result_) result_ = consumeToken(builder_, "UNION");
    if (!result_) result_ = consumeToken(builder_, "INTERSECT");
    if (!result_) result_ = consumeToken(builder_, "EXCEPT");
    if (!result_) result_ = consumeToken(builder_, "MINUS");
    return result_;
  }

  /* ********************************************************** */
  // "REFRESH" refresh_mode?
  public static boolean refresh_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refresh_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REFRESH_CLAUSE, "<refresh clause>");
    result_ = consumeToken(builder_, "REFRESH");
    pinned_ = result_; // pin = 1
    result_ = result_ && refresh_clause_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // refresh_mode?
  private static boolean refresh_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refresh_clause_1")) return false;
    refresh_mode(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "REFRESH" "MATERIALIZED" "VIEW" materialized_view_reference ("PARTITION" parenthesized_value_expression)?
  public static boolean refresh_materialized_view_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refresh_materialized_view_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REFRESH_MATERIALIZED_VIEW_STATEMENT, "<refresh materialized view statement>");
    result_ = consumeToken(builder_, "REFRESH");
    result_ = result_ && consumeToken(builder_, "MATERIALIZED");
    result_ = result_ && consumeToken(builder_, "VIEW");
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, materialized_view_reference(builder_, level_ + 1));
    result_ = pinned_ && refresh_materialized_view_statement_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("PARTITION" parenthesized_value_expression)?
  private static boolean refresh_materialized_view_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refresh_materialized_view_statement_4")) return false;
    refresh_materialized_view_statement_4_0(builder_, level_ + 1);
    return true;
  }

  // "PARTITION" parenthesized_value_expression
  private static boolean refresh_materialized_view_statement_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refresh_materialized_view_statement_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "PARTITION");
    result_ = result_ && parenthesized_value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "ASYNC" | "MANUAL" | "IMMEDIATE" | "DEFERRED"
  static boolean refresh_mode(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refresh_mode")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "ASYNC");
    if (!result_) result_ = consumeToken(builder_, "MANUAL");
    if (!result_) result_ = consumeToken(builder_, "IMMEDIATE");
    if (!result_) result_ = consumeToken(builder_, "DEFERRED");
    return result_;
  }

  /* ********************************************************** */
  // "RENAME" ("TO" identifier_reference)?
  public static boolean rename_action(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rename_action")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RENAME_ACTION, "<rename action>");
    result_ = consumeToken(builder_, "RENAME");
    result_ = result_ && rename_action_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("TO" identifier_reference)?
  private static boolean rename_action_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rename_action_1")) return false;
    rename_action_1_0(builder_, level_ + 1);
    return true;
  }

  // "TO" identifier_reference
  private static boolean rename_action_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rename_action_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "TO");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean resource_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "resource_reference")) return false;
    if (!nextTokenIs(builder_, "<resource reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RESOURCE_REFERENCE, "<resource reference>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "REVOKE" privilege_list "ON" privilege_target "FROM" security_principal_list
  public static boolean revoke_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "revoke_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REVOKE_STATEMENT, "<revoke statement>");
    result_ = consumeToken(builder_, "REVOKE");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, privilege_list(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "ON")) && result_;
    result_ = pinned_ && report_error_(builder_, privilege_target(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "FROM")) && result_;
    result_ = pinned_ && security_principal_list(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "ROLLBACK"
  public static boolean rollback_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rollback_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_ROLLBACK_STATEMENT, "<rollback statement>");
    result_ = consumeToken(builder_, "ROLLBACK");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // columns_clause | properties_clause | from_kafka_clause
  public static boolean routine_load_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "routine_load_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ROUTINE_LOAD_CLAUSE, "<routine load clause>");
    result_ = columns_clause(builder_, level_ + 1);
    if (!result_) result_ = properties_clause(builder_, level_ + 1);
    if (!result_) result_ = from_kafka_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "BOOLEAN" | "BOOL" | "TINYINT" | "SMALLINT" | "INT" | "INTEGER" | "BIGINT" | "LARGEINT" | "FLOAT" | "DOUBLE" | "DECIMAL" | "DECIMALV2" | "DECIMAL32" | "DECIMAL64" | "DECIMAL128" | "CHAR" | "VARCHAR" | "VARCHAR2" | "STRING" | "TEXT" | "DATE" | "DATETIME" | "TIME" | "TIMESTAMP" | "JSON"
  public static boolean scalar_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "scalar_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SCALAR_TYPE, "<scalar type>");
    result_ = consumeToken(builder_, "BOOLEAN");
    if (!result_) result_ = consumeToken(builder_, "BOOL");
    if (!result_) result_ = consumeToken(builder_, "TINYINT");
    if (!result_) result_ = consumeToken(builder_, "SMALLINT");
    if (!result_) result_ = consumeToken(builder_, "INT");
    if (!result_) result_ = consumeToken(builder_, "INTEGER");
    if (!result_) result_ = consumeToken(builder_, "BIGINT");
    if (!result_) result_ = consumeToken(builder_, "LARGEINT");
    if (!result_) result_ = consumeToken(builder_, "FLOAT");
    if (!result_) result_ = consumeToken(builder_, "DOUBLE");
    if (!result_) result_ = consumeToken(builder_, "DECIMAL");
    if (!result_) result_ = consumeToken(builder_, "DECIMALV2");
    if (!result_) result_ = consumeToken(builder_, "DECIMAL32");
    if (!result_) result_ = consumeToken(builder_, "DECIMAL64");
    if (!result_) result_ = consumeToken(builder_, "DECIMAL128");
    if (!result_) result_ = consumeToken(builder_, "CHAR");
    if (!result_) result_ = consumeToken(builder_, "VARCHAR");
    if (!result_) result_ = consumeToken(builder_, "VARCHAR2");
    if (!result_) result_ = consumeToken(builder_, "STRING");
    if (!result_) result_ = consumeToken(builder_, "TEXT");
    if (!result_) result_ = consumeToken(builder_, "DATE");
    if (!result_) result_ = consumeToken(builder_, "DATETIME");
    if (!result_) result_ = consumeToken(builder_, "TIME");
    if (!result_) result_ = consumeToken(builder_, "TIMESTAMP");
    if (!result_) result_ = consumeToken(builder_, "JSON");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean schema_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "schema_reference")) return false;
    if (!nextTokenIs(builder_, "<schema reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SCHEMA_REFERENCE, "<schema reference>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // statement (statement_separator statement)* statement_separator? <<eof>>
  static boolean script(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "script")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = statement(builder_, level_ + 1);
    result_ = result_ && script_1(builder_, level_ + 1);
    result_ = result_ && script_2(builder_, level_ + 1);
    result_ = result_ && eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (statement_separator statement)*
  private static boolean script_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "script_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!script_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "script_1", pos_)) break;
    }
    return true;
  }

  // statement_separator statement
  private static boolean script_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "script_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = statement_separator(builder_, level_ + 1);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // statement_separator?
  private static boolean script_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "script_2")) return false;
    statement_separator(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier_reference ("@" identifier_reference)?
  public static boolean security_principal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "security_principal")) return false;
    if (!nextTokenIs(builder_, "<security principal>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECURITY_PRINCIPAL, "<security principal>");
    result_ = identifier_reference(builder_, level_ + 1);
    result_ = result_ && security_principal_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("@" identifier_reference)?
  private static boolean security_principal_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "security_principal_1")) return false;
    security_principal_1_0(builder_, level_ + 1);
    return true;
  }

  // "@" identifier_reference
  private static boolean security_principal_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "security_principal_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "@");
    result_ = result_ && identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // security_principal ("," security_principal)*
  public static boolean security_principal_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "security_principal_list")) return false;
    if (!nextTokenIs(builder_, "<security principal list>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECURITY_PRINCIPAL_LIST, "<security principal list>");
    result_ = security_principal(builder_, level_ + 1);
    result_ = result_ && security_principal_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," security_principal)*
  private static boolean security_principal_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "security_principal_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!security_principal_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "security_principal_list_1", pos_)) break;
    }
    return true;
  }

  // "," security_principal
  private static boolean security_principal_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "security_principal_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && security_principal(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean select_alias(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_alias")) return false;
    if (!nextTokenIs(builder_, "<select alias>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SELECT_ALIAS, "<select alias>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "AS"? select_alias
  static boolean select_alias_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_alias_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = select_alias_clause_0(builder_, level_ + 1);
    result_ = result_ && select_alias(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "AS"?
  private static boolean select_alias_clause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_alias_clause_0")) return false;
    consumeToken(builder_, "AS");
    return true;
  }

  /* ********************************************************** */
  // "SELECT" select_modifier* select_item ("," select_item)*
  public static boolean select_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_SELECT_CLAUSE, "<select clause>");
    result_ = consumeToken(builder_, "SELECT");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, select_clause_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, select_item(builder_, level_ + 1)) && result_;
    result_ = pinned_ && select_clause_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::select_recover);
    return result_ || pinned_;
  }

  // select_modifier*
  private static boolean select_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_clause_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!select_modifier(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "select_clause_1", pos_)) break;
    }
    return true;
  }

  // ("," select_item)*
  private static boolean select_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_clause_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!select_clause_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "select_clause_3", pos_)) break;
    }
    return true;
  }

  // "," select_item
  private static boolean select_clause_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_clause_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && select_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // value_expression select_alias_clause?
  public static boolean select_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SELECT_ITEM, "<select item>");
    result_ = value_expression(builder_, level_ + 1);
    result_ = result_ && select_item_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // select_alias_clause?
  private static boolean select_item_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_item_1")) return false;
    select_alias_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "ALL" | "DISTINCT"
  static boolean select_modifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_modifier")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "ALL");
    if (!result_) result_ = consumeToken(builder_, "DISTINCT");
    return result_;
  }

  /* ********************************************************** */
  // !(";" | "FROM" | "WHERE" | "GROUP" | "HAVING" | "QUALIFY" | "WINDOW" | "ORDER" | "LIMIT")
  static boolean select_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !select_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ";" | "FROM" | "WHERE" | "GROUP" | "HAVING" | "QUALIFY" | "WINDOW" | "ORDER" | "LIMIT"
  private static boolean select_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ";");
    if (!result_) result_ = consumeToken(builder_, "FROM");
    if (!result_) result_ = consumeToken(builder_, "WHERE");
    if (!result_) result_ = consumeToken(builder_, "GROUP");
    if (!result_) result_ = consumeToken(builder_, "HAVING");
    if (!result_) result_ = consumeToken(builder_, "QUALIFY");
    if (!result_) result_ = consumeToken(builder_, "WINDOW");
    if (!result_) result_ = consumeToken(builder_, "ORDER");
    if (!result_) result_ = consumeToken(builder_, "LIMIT");
    return result_;
  }

  /* ********************************************************** */
  // query_expression
  public static boolean select_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_SELECT_STATEMENT, "<select statement>");
    result_ = query_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::statement_recover);
    return result_;
  }

  /* ********************************************************** */
  // column_reference "=" value_expression
  public static boolean set_assignment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_assignment")) return false;
    if (!nextTokenIs(builder_, "<set assignment>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET_ASSIGNMENT, "<set assignment>");
    result_ = column_reference(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "=");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "SET" set_assignment ("," set_assignment)*
  public static boolean set_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_SET_CLAUSE, "<set clause>");
    result_ = consumeToken(builder_, "SET");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, set_assignment(builder_, level_ + 1));
    result_ = pinned_ && set_clause_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("," set_assignment)*
  private static boolean set_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_clause_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!set_clause_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "set_clause_2", pos_)) break;
    }
    return true;
  }

  // "," set_assignment
  private static boolean set_clause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_clause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && set_assignment(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // set_operator simple_query_expression
  public static boolean set_operation_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_operation_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET_OPERATION_CLAUSE, "<set operation clause>");
    result_ = set_operator(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && simple_query_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ("UNION" "ALL"?) | "INTERSECT" | "EXCEPT" | "MINUS"
  public static boolean set_operator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_operator")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET_OPERATOR, "<set operator>");
    result_ = set_operator_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, "INTERSECT");
    if (!result_) result_ = consumeToken(builder_, "EXCEPT");
    if (!result_) result_ = consumeToken(builder_, "MINUS");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "UNION" "ALL"?
  private static boolean set_operator_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_operator_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "UNION");
    result_ = result_ && set_operator_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "ALL"?
  private static boolean set_operator_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_operator_0_1")) return false;
    consumeToken(builder_, "ALL");
    return true;
  }

  /* ********************************************************** */
  // "SET" "PASSWORD" ("FOR" security_principal)? "=" value_expression
  public static boolean set_password_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_password_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET_PASSWORD_STATEMENT, "<set password statement>");
    result_ = consumeToken(builder_, "SET");
    result_ = result_ && consumeToken(builder_, "PASSWORD");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, set_password_statement_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "=")) && result_;
    result_ = pinned_ && value_expression(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("FOR" security_principal)?
  private static boolean set_password_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_password_statement_2")) return false;
    set_password_statement_2_0(builder_, level_ + 1);
    return true;
  }

  // "FOR" security_principal
  private static boolean set_password_statement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_password_statement_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "FOR");
    result_ = result_ && security_principal(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "SET" properties_clause
  public static boolean set_properties_action(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_properties_action")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET_PROPERTIES_ACTION, "<set properties action>");
    result_ = consumeToken(builder_, "SET");
    result_ = result_ && properties_clause(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // simple_query_expression set_operation_clause+
  public static boolean set_query_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_query_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET_QUERY_EXPRESSION, "<set query expression>");
    result_ = simple_query_expression(builder_, level_ + 1);
    result_ = result_ && set_query_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::query_recover);
    return result_;
  }

  // set_operation_clause+
  private static boolean set_query_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_query_expression_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = set_operation_clause(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!set_operation_clause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "set_query_expression_1", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "SET" set_assignment ("," set_assignment)*
  public static boolean set_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_SET_STATEMENT, "<set statement>");
    result_ = consumeToken(builder_, "SET");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, set_assignment(builder_, level_ + 1));
    result_ = pinned_ && set_statement_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // ("," set_assignment)*
  private static boolean set_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_statement_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!set_statement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "set_statement_2", pos_)) break;
    }
    return true;
  }

  // "," set_assignment
  private static boolean set_statement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_statement_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && set_assignment(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ("TABLE" table_reference) | ("VIEW" view_reference) | ("MATERIALIZED" "VIEW" materialized_view_reference) | ("CATALOG" catalog_reference)
  public static boolean show_create_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_create_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SHOW_CREATE_TARGET, "<show create target>");
    result_ = show_create_target_0(builder_, level_ + 1);
    if (!result_) result_ = show_create_target_1(builder_, level_ + 1);
    if (!result_) result_ = show_create_target_2(builder_, level_ + 1);
    if (!result_) result_ = show_create_target_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "TABLE" table_reference
  private static boolean show_create_target_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_create_target_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "TABLE");
    result_ = result_ && table_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "VIEW" view_reference
  private static boolean show_create_target_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_create_target_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "VIEW");
    result_ = result_ && view_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "MATERIALIZED" "VIEW" materialized_view_reference
  private static boolean show_create_target_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_create_target_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "MATERIALIZED");
    result_ = result_ && consumeToken(builder_, "VIEW");
    result_ = result_ && materialized_view_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "CATALOG" catalog_reference
  private static boolean show_create_target_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_create_target_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "CATALOG");
    result_ = result_ && catalog_reference(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "SHOW" show_target
  public static boolean show_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SHOW_STATEMENT, "<show statement>");
    result_ = consumeToken(builder_, "SHOW");
    pinned_ = result_; // pin = 1
    result_ = result_ && show_target(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "DATABASES" | "SCHEMAS" | "TABLES" | "PARTITIONS" | "VARIABLES" | "ROLES" | "USERS" | "FUNCTIONS" | "GRANTS" | "CATALOGS" | "RESOURCES" | "MATERIALIZED" "VIEWS" | "CREATE" show_create_target | "PROC" value_expression?
  public static boolean show_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SHOW_TARGET, "<show target>");
    result_ = consumeToken(builder_, "DATABASES");
    if (!result_) result_ = consumeToken(builder_, "SCHEMAS");
    if (!result_) result_ = consumeToken(builder_, "TABLES");
    if (!result_) result_ = consumeToken(builder_, "PARTITIONS");
    if (!result_) result_ = consumeToken(builder_, "VARIABLES");
    if (!result_) result_ = consumeToken(builder_, "ROLES");
    if (!result_) result_ = consumeToken(builder_, "USERS");
    if (!result_) result_ = consumeToken(builder_, "FUNCTIONS");
    if (!result_) result_ = consumeToken(builder_, "GRANTS");
    if (!result_) result_ = consumeToken(builder_, "CATALOGS");
    if (!result_) result_ = consumeToken(builder_, "RESOURCES");
    if (!result_) result_ = show_target_11(builder_, level_ + 1);
    if (!result_) result_ = show_target_12(builder_, level_ + 1);
    if (!result_) result_ = show_target_13(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "MATERIALIZED" "VIEWS"
  private static boolean show_target_11(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_target_11")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "MATERIALIZED");
    result_ = result_ && consumeToken(builder_, "VIEWS");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "CREATE" show_create_target
  private static boolean show_target_12(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_target_12")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "CREATE");
    result_ = result_ && show_create_target(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "PROC" value_expression?
  private static boolean show_target_13(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_target_13")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, "PROC");
    result_ = result_ && show_target_13_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // value_expression?
  private static boolean show_target_13_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "show_target_13_1")) return false;
    value_expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // select_clause from_clause? where_clause? group_by_clause? having_clause? qualify_clause? window_clause? order_by_clause? limit_clause?
  public static boolean simple_query_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SIMPLE_QUERY_EXPRESSION, "<simple query expression>");
    result_ = select_clause(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, simple_query_expression_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, simple_query_expression_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, simple_query_expression_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, simple_query_expression_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, simple_query_expression_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, simple_query_expression_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, simple_query_expression_7(builder_, level_ + 1)) && result_;
    result_ = pinned_ && simple_query_expression_8(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // from_clause?
  private static boolean simple_query_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_1")) return false;
    from_clause(builder_, level_ + 1);
    return true;
  }

  // where_clause?
  private static boolean simple_query_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_2")) return false;
    where_clause(builder_, level_ + 1);
    return true;
  }

  // group_by_clause?
  private static boolean simple_query_expression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_3")) return false;
    group_by_clause(builder_, level_ + 1);
    return true;
  }

  // having_clause?
  private static boolean simple_query_expression_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_4")) return false;
    having_clause(builder_, level_ + 1);
    return true;
  }

  // qualify_clause?
  private static boolean simple_query_expression_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_5")) return false;
    qualify_clause(builder_, level_ + 1);
    return true;
  }

  // window_clause?
  private static boolean simple_query_expression_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_6")) return false;
    window_clause(builder_, level_ + 1);
    return true;
  }

  // order_by_clause?
  private static boolean simple_query_expression_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_7")) return false;
    order_by_clause(builder_, level_ + 1);
    return true;
  }

  // limit_clause?
  private static boolean simple_query_expression_8(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_query_expression_8")) return false;
    limit_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ddl_statement | dml_statement | other_statement
  public static boolean statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT, "<statement>");
    result_ = ddl_statement(builder_, level_ + 1);
    if (!result_) result_ = dml_statement(builder_, level_ + 1);
    if (!result_) result_ = other_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::statement_recover);
    return result_;
  }

  /* ********************************************************** */
  // !(";" | ddl_statement_start | dml_statement_start | other_statement_start)
  static boolean statement_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !statement_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ";" | ddl_statement_start | dml_statement_start | other_statement_start
  private static boolean statement_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ";");
    if (!result_) result_ = ddl_statement_start(builder_, level_ + 1);
    if (!result_) result_ = dml_statement_start(builder_, level_ + 1);
    if (!result_) result_ = other_statement_start(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // ";"
  static boolean statement_separator(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, ";");
  }

  /* ********************************************************** */
  // SQL_STRING_TOKEN
  public static boolean string_literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_literal")) return false;
    if (!nextTokenIs(builder_, SQL_STRING_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SQL_STRING_TOKEN);
    exit_section_(builder_, marker_, STRING_LITERAL, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference ":" type_element
  public static boolean struct_field(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "struct_field")) return false;
    if (!nextTokenIs(builder_, "<struct field>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRUCT_FIELD, "<struct field>");
    result_ = identifier_reference(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ":");
    result_ = result_ && type_element(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "STRUCT" "<" struct_field ("," struct_field)* ">"
  public static boolean struct_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "struct_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRUCT_TYPE, "<struct type>");
    result_ = consumeToken(builder_, "STRUCT");
    result_ = result_ && consumeToken(builder_, "<");
    result_ = result_ && struct_field(builder_, level_ + 1);
    result_ = result_ && struct_type_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ">");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," struct_field)*
  private static boolean struct_type_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "struct_type_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!struct_type_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "struct_type_3", pos_)) break;
    }
    return true;
  }

  // "," struct_field
  private static boolean struct_type_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "struct_type_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && struct_field(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "SYNC"
  public static boolean sync_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sync_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SYNC_STATEMENT, "<sync statement>");
    result_ = consumeToken(builder_, "SYNC");
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::statement_recover);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean table_alias(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias")) return false;
    if (!nextTokenIs(builder_, "<table alias>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_ALIAS, "<table alias>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "AS"? table_alias table_alias_column_list?
  static boolean table_alias_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = table_alias_clause_0(builder_, level_ + 1);
    result_ = result_ && table_alias(builder_, level_ + 1);
    result_ = result_ && table_alias_clause_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "AS"?
  private static boolean table_alias_clause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_clause_0")) return false;
    consumeToken(builder_, "AS");
    return true;
  }

  // table_alias_column_list?
  private static boolean table_alias_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_clause_2")) return false;
    table_alias_column_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "(" table_alias_column_name ("," table_alias_column_name)* ","? ")"
  public static boolean table_alias_column_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_column_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_ALIAS_COLUMN_LIST, "<table alias column list>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && table_alias_column_name(builder_, level_ + 1);
    result_ = result_ && table_alias_column_list_2(builder_, level_ + 1);
    result_ = result_ && table_alias_column_list_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," table_alias_column_name)*
  private static boolean table_alias_column_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_column_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!table_alias_column_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "table_alias_column_list_2", pos_)) break;
    }
    return true;
  }

  // "," table_alias_column_name
  private static boolean table_alias_column_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_column_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && table_alias_column_name(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ","?
  private static boolean table_alias_column_list_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_column_list_3")) return false;
    consumeToken(builder_, ",");
    return true;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean table_alias_column_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_alias_column_name")) return false;
    if (!nextTokenIs(builder_, "<table alias column name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_ALIAS_COLUMN_NAME, "<table alias column name>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // column_definition | table_constraint
  public static boolean table_column_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_COLUMN_ITEM, "<table column item>");
    result_ = column_definition(builder_, level_ + 1);
    if (!result_) result_ = table_constraint(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "(" table_column_item ("," table_column_item)* ","? ")"
  public static boolean table_column_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_list")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_TABLE_ELEMENT_LIST, "<table column list>");
    result_ = consumeToken(builder_, "(");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, table_column_item(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, table_column_list_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, table_column_list_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ")") && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::table_column_recover);
    return result_ || pinned_;
  }

  // ("," table_column_item)*
  private static boolean table_column_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!table_column_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "table_column_list_2", pos_)) break;
    }
    return true;
  }

  // "," table_column_item
  private static boolean table_column_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && table_column_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ","?
  private static boolean table_column_list_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_list_3")) return false;
    consumeToken(builder_, ",");
    return true;
  }

  /* ********************************************************** */
  // !(")" | "," | ";")
  static boolean table_column_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !table_column_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ")" | "," | ";"
  private static boolean table_column_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_column_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ")");
    if (!result_) result_ = consumeToken(builder_, ",");
    if (!result_) result_ = consumeToken(builder_, ";");
    return result_;
  }

  /* ********************************************************** */
  // ("PRIMARY" | "UNIQUE" | "DUPLICATE" | "AGGREGATE") "KEY" key_column_list
  public static boolean table_constraint(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_constraint")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_CONSTRAINT, "<table constraint>");
    result_ = table_constraint_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "KEY");
    result_ = result_ && key_column_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "PRIMARY" | "UNIQUE" | "DUPLICATE" | "AGGREGATE"
  private static boolean table_constraint_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_constraint_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "PRIMARY");
    if (!result_) result_ = consumeToken(builder_, "UNIQUE");
    if (!result_) result_ = consumeToken(builder_, "DUPLICATE");
    if (!result_) result_ = consumeToken(builder_, "AGGREGATE");
    return result_;
  }

  /* ********************************************************** */
  // table_reference table_alias_clause? join_expression*
  //   | parenthesized_query_expression table_alias_clause?
  //   | table_function_call table_alias_clause?
  //   | parenthesized_join_expression table_alias_clause?
  public static boolean table_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_TABLE_EXPRESSION, "<table expression>");
    result_ = table_expression_0(builder_, level_ + 1);
    if (!result_) result_ = table_expression_1(builder_, level_ + 1);
    if (!result_) result_ = table_expression_2(builder_, level_ + 1);
    if (!result_) result_ = table_expression_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // table_reference table_alias_clause? join_expression*
  private static boolean table_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = table_reference(builder_, level_ + 1);
    result_ = result_ && table_expression_0_1(builder_, level_ + 1);
    result_ = result_ && table_expression_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // table_alias_clause?
  private static boolean table_expression_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_0_1")) return false;
    table_alias_clause(builder_, level_ + 1);
    return true;
  }

  // join_expression*
  private static boolean table_expression_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!join_expression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "table_expression_0_2", pos_)) break;
    }
    return true;
  }

  // parenthesized_query_expression table_alias_clause?
  private static boolean table_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parenthesized_query_expression(builder_, level_ + 1);
    result_ = result_ && table_expression_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // table_alias_clause?
  private static boolean table_expression_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_1_1")) return false;
    table_alias_clause(builder_, level_ + 1);
    return true;
  }

  // table_function_call table_alias_clause?
  private static boolean table_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = table_function_call(builder_, level_ + 1);
    result_ = result_ && table_expression_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // table_alias_clause?
  private static boolean table_expression_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_2_1")) return false;
    table_alias_clause(builder_, level_ + 1);
    return true;
  }

  // parenthesized_join_expression table_alias_clause?
  private static boolean table_expression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parenthesized_join_expression(builder_, level_ + 1);
    result_ = result_ && table_expression_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // table_alias_clause?
  private static boolean table_expression_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_expression_3_1")) return false;
    table_alias_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "UNNEST" "(" value_expression ")"
  public static boolean table_function_call(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_function_call")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_FUNCTION_CALL, "<table function call>");
    result_ = consumeToken(builder_, "UNNEST");
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // table_reference_name
  public static boolean table_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_reference")) return false;
    if (!nextTokenIs(builder_, "<table reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_TABLE_REFERENCE, "<table reference>");
    result_ = table_reference_name(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean table_reference_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_reference_name")) return false;
    if (!nextTokenIs(builder_, "<table reference name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TABLE_REFERENCE_NAME, "<table reference name>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "TEMPORARY" | "EXTERNAL"
  static boolean table_scope(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "table_scope")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "TEMPORARY");
    if (!result_) result_ = consumeToken(builder_, "EXTERNAL");
    return result_;
  }

  /* ********************************************************** */
  // "SUBMIT" "TASK" identifier_reference "AS" (refresh_materialized_view_statement | query_expression)
  public static boolean task_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "task_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TASK_STATEMENT, "<task statement>");
    result_ = consumeToken(builder_, "SUBMIT");
    result_ = result_ && consumeToken(builder_, "TASK");
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier_reference(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, "AS")) && result_;
    result_ = pinned_ && task_statement_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // refresh_materialized_view_statement | query_expression
  private static boolean task_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "task_statement_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = refresh_materialized_view_statement(builder_, level_ + 1);
    if (!result_) result_ = query_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // begin_statement | commit_statement | rollback_statement
  static boolean transaction_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "transaction_statement")) return false;
    boolean result_;
    result_ = begin_statement(builder_, level_ + 1);
    if (!result_) result_ = commit_statement(builder_, level_ + 1);
    if (!result_) result_ = rollback_statement(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // "TRUNCATE" "TABLE" table_reference
  public static boolean truncate_table_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "truncate_table_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_TRUNCATE_TABLE_STATEMENT, "<truncate table statement>");
    result_ = consumeToken(builder_, "TRUNCATE");
    result_ = result_ && consumeToken(builder_, "TABLE");
    pinned_ = result_; // pin = 2
    result_ = result_ && table_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // scalar_type type_parameters? | complex_type
  public static boolean type_element(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_element")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_TYPE_ELEMENT, "<type element>");
    result_ = type_element_0(builder_, level_ + 1);
    if (!result_) result_ = complex_type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::type_recover);
    return result_;
  }

  // scalar_type type_parameters?
  private static boolean type_element_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_element_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = scalar_type(builder_, level_ + 1);
    result_ = result_ && type_element_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_parameters?
  private static boolean type_element_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_element_0_1")) return false;
    type_parameters(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "(" numeric_literal ("," numeric_literal)* ")"
  public static boolean type_parameters(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_parameters")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_PARAMETERS, "<type parameters>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && numeric_literal(builder_, level_ + 1);
    result_ = result_ && type_parameters_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," numeric_literal)*
  private static boolean type_parameters_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_parameters_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!type_parameters_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "type_parameters_2", pos_)) break;
    }
    return true;
  }

  // "," numeric_literal
  private static boolean type_parameters_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_parameters_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && numeric_literal(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !("," | ")" | "NULL" | "NOT" | "DEFAULT" | "COMMENT" | "AS")
  static boolean type_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !type_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "," | ")" | "NULL" | "NOT" | "DEFAULT" | "COMMENT" | "AS"
  private static boolean type_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ",");
    if (!result_) result_ = consumeToken(builder_, ")");
    if (!result_) result_ = consumeToken(builder_, "NULL");
    if (!result_) result_ = consumeToken(builder_, "NOT");
    if (!result_) result_ = consumeToken(builder_, "DEFAULT");
    if (!result_) result_ = consumeToken(builder_, "COMMENT");
    if (!result_) result_ = consumeToken(builder_, "AS");
    return result_;
  }

  /* ********************************************************** */
  // typed_literal_prefix literal
  public static boolean typed_literal_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typed_literal_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPED_LITERAL_EXPRESSION, "<typed literal expression>");
    result_ = typed_literal_prefix(builder_, level_ + 1);
    result_ = result_ && literal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "DATE" | "DATETIME" | "TIME" | "TIMESTAMP"
  public static boolean typed_literal_prefix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typed_literal_prefix")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPED_LITERAL_PREFIX, "<typed literal prefix>");
    result_ = consumeToken(builder_, "DATE");
    if (!result_) result_ = consumeToken(builder_, "DATETIME");
    if (!result_) result_ = consumeToken(builder_, "TIME");
    if (!result_) result_ = consumeToken(builder_, "TIMESTAMP");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ("+" | "-" | "!" | "~" | "BINARY")* postfix_expression
  public static boolean unary_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNARY_EXPRESSION, "<unary expression>");
    result_ = unary_expression_0(builder_, level_ + 1);
    result_ = result_ && postfix_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("+" | "-" | "!" | "~" | "BINARY")*
  private static boolean unary_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expression_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!unary_expression_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unary_expression_0", pos_)) break;
    }
    return true;
  }

  // "+" | "-" | "!" | "~" | "BINARY"
  private static boolean unary_expression_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expression_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, "+");
    if (!result_) result_ = consumeToken(builder_, "-");
    if (!result_) result_ = consumeToken(builder_, "!");
    if (!result_) result_ = consumeToken(builder_, "~");
    if (!result_) result_ = consumeToken(builder_, "BINARY");
    return result_;
  }

  /* ********************************************************** */
  // "UNSET" "VARIABLE"? identifier_reference
  public static boolean unset_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unset_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNSET_STATEMENT, "<unset statement>");
    result_ = consumeToken(builder_, "UNSET");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, unset_statement_1(builder_, level_ + 1));
    result_ = pinned_ && identifier_reference(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // "VARIABLE"?
  private static boolean unset_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unset_statement_1")) return false;
    consumeToken(builder_, "VARIABLE");
    return true;
  }

  /* ********************************************************** */
  // "UPDATE" dml_target_table set_clause from_clause? where_clause?
  public static boolean update_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "update_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_UPDATE_STATEMENT, "<update statement>");
    result_ = consumeToken(builder_, "UPDATE");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, dml_target_table(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, set_clause(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, update_statement_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && update_statement_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  // from_clause?
  private static boolean update_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "update_statement_3")) return false;
    from_clause(builder_, level_ + 1);
    return true;
  }

  // where_clause?
  private static boolean update_statement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "update_statement_4")) return false;
    where_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "USE" use_target
  public static boolean use_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "use_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_USE_SCHEMA_STATEMENT, "<use statement>");
    result_ = consumeToken(builder_, "USE");
    pinned_ = result_; // pin = 1
    result_ = result_ && use_target(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::statement_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // schema_reference
  public static boolean use_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "use_target")) return false;
    if (!nextTokenIs(builder_, "<use target>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, USE_TARGET, "<use target>");
    result_ = schema_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "USING" parenthesized_identifier_list
  public static boolean using_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "using_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_USING_CLAUSE, "<using clause>");
    result_ = consumeToken(builder_, "USING");
    result_ = result_ && parenthesized_identifier_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // or_expression
  public static boolean value_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "value_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VALUE_EXPRESSION, "<value expression>");
    result_ = or_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, StarRocksGeneratedParser::expression_recover);
    return result_;
  }

  /* ********************************************************** */
  // "VALUES" values_row ("," values_row)*
  public static boolean values_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_VALUES_EXPRESSION, "<values clause>");
    result_ = consumeToken(builder_, "VALUES");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, values_row(builder_, level_ + 1));
    result_ = pinned_ && values_clause_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // ("," values_row)*
  private static boolean values_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_clause_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!values_clause_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "values_clause_2", pos_)) break;
    }
    return true;
  }

  // "," values_row
  private static boolean values_clause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_clause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && values_row(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // values_clause order_by_clause? limit_clause?
  public static boolean values_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VALUES_EXPRESSION, "<values expression>");
    result_ = values_clause(builder_, level_ + 1);
    result_ = result_ && values_expression_1(builder_, level_ + 1);
    result_ = result_ && values_expression_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // order_by_clause?
  private static boolean values_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_expression_1")) return false;
    order_by_clause(builder_, level_ + 1);
    return true;
  }

  // limit_clause?
  private static boolean values_expression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_expression_2")) return false;
    limit_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "(" value_expression ("," value_expression)* ")"
  public static boolean values_row(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_row")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VALUES_ROW, "<values row>");
    result_ = consumeToken(builder_, "(");
    result_ = result_ && value_expression(builder_, level_ + 1);
    result_ = result_ && values_row_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ("," value_expression)*
  private static boolean values_row_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_row_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!values_row_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "values_row_2", pos_)) break;
    }
    return true;
  }

  // "," value_expression
  private static boolean values_row_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "values_row_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // qualified_identifier
  public static boolean view_reference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "view_reference")) return false;
    if (!nextTokenIs(builder_, "<view reference>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VIEW_REFERENCE, "<view reference>");
    result_ = qualified_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "WHERE" value_expression
  public static boolean where_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "where_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_WHERE_CLAUSE, "<where clause>");
    result_ = consumeToken(builder_, "WHERE");
    pinned_ = result_; // pin = 1
    result_ = result_ && value_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "WINDOW" window_definition ("," window_definition)*
  public static boolean window_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_WINDOW_CLAUSE, "<window clause>");
    result_ = consumeToken(builder_, "WINDOW");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, window_definition(builder_, level_ + 1));
    result_ = pinned_ && window_clause_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // ("," window_definition)*
  private static boolean window_clause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_clause_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!window_clause_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "window_clause_2", pos_)) break;
    }
    return true;
  }

  // "," window_definition
  private static boolean window_clause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_clause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && window_definition(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // window_name "AS" "(" window_specification ")"
  public static boolean window_definition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_definition")) return false;
    if (!nextTokenIs(builder_, "<window definition>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WINDOW_DEFINITION, "<window definition>");
    result_ = window_name(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, "AS");
    result_ = result_ && consumeToken(builder_, "(");
    result_ = result_ && window_specification(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ")");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean window_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_name")) return false;
    if (!nextTokenIs(builder_, "<window name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WINDOW_NAME, "<window name>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier_reference
  public static boolean window_reference_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_reference_name")) return false;
    if (!nextTokenIs(builder_, "<window reference name>", SQL_IDENT, SQL_IDENT_DELIMITED)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WINDOW_REFERENCE_NAME, "<window reference name>");
    result_ = identifier_reference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // partition_by_clause? order_by_clause? frame_clause?
  public static boolean window_specification(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_specification")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WINDOW_SPECIFICATION, "<window specification>");
    result_ = window_specification_0(builder_, level_ + 1);
    result_ = result_ && window_specification_1(builder_, level_ + 1);
    result_ = result_ && window_specification_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // partition_by_clause?
  private static boolean window_specification_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_specification_0")) return false;
    partition_by_clause(builder_, level_ + 1);
    return true;
  }

  // order_by_clause?
  private static boolean window_specification_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_specification_1")) return false;
    order_by_clause(builder_, level_ + 1);
    return true;
  }

  // frame_clause?
  private static boolean window_specification_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "window_specification_2")) return false;
    frame_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // "WITH" "RECURSIVE"? named_query_definition ("," named_query_definition)*
  public static boolean with_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_clause")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SQL_WITH_CLAUSE, "<with clause>");
    result_ = consumeToken(builder_, "WITH");
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, with_clause_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, named_query_definition(builder_, level_ + 1)) && result_;
    result_ = pinned_ && with_clause_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // "RECURSIVE"?
  private static boolean with_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_clause_1")) return false;
    consumeToken(builder_, "RECURSIVE");
    return true;
  }

  // ("," named_query_definition)*
  private static boolean with_clause_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_clause_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!with_clause_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "with_clause_3", pos_)) break;
    }
    return true;
  }

  // "," named_query_definition
  private static boolean with_clause_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_clause_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ",");
    result_ = result_ && named_query_definition(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "WITH" "GRANT" "OPTION"
  public static boolean with_grant_option_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_grant_option_clause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WITH_GRANT_OPTION_CLAUSE, "<with grant option clause>");
    result_ = consumeToken(builder_, "WITH");
    result_ = result_ && consumeToken(builder_, "GRANT");
    result_ = result_ && consumeToken(builder_, "OPTION");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // with_clause (set_query_expression | simple_query_expression | values_expression)
  public static boolean with_query_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_query_expression")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WITH_QUERY_EXPRESSION, "<with query expression>");
    result_ = with_clause(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && with_query_expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, StarRocksGeneratedParser::query_recover);
    return result_ || pinned_;
  }

  // set_query_expression | simple_query_expression | values_expression
  private static boolean with_query_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "with_query_expression_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = set_query_expression(builder_, level_ + 1);
    if (!result_) result_ = simple_query_expression(builder_, level_ + 1);
    if (!result_) result_ = values_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

}
